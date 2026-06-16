package pt.isep.sem4pi.application.service.weatherdata.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherTimeFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Estrategia de parsing XLSX usada pela US042.
 *
 * <p>O importador percorre todas as folhas ate encontrar o cabecalho suportado
 * de dados de vento. O Apache POI e usado para tratar de forma consistente
 * datas/horas numericas do Excel e valores textuais. Tal como no importador
 * CSV, a altitude e convertida a partir do ponto medio do intervalo de flight
 * levels e as medicoes indisponiveis recebem valores de referencia.</p>
 */
public class WeatherDataXLSXImporter implements WeatherDataImporter {

    private static final double DEFAULT_TEMPERATURE_C = 15.0;
    private static final double DEFAULT_PRESSURE_HPA = 1013.25;
    private static final double DEFAULT_HUMIDITY_PCT = 50.0;
    private static final double FEET_TO_METERS = 0.3048;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private static final String COL_ACA = "aca";
    private static final String COL_ALT_INF = "alt_inf(ft)";
    private static final String COL_ALT_SUP = "alt_sup(ft)";
    private static final String COL_DIRECTION = "direction(degree)";
    private static final String COL_VALUE = "value(knot)";
    private static final String COL_DAY = "day";
    private static final String COL_START = "star";
    private static final String COL_END = "end";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        Path path = Path.of(filePath);
        return Files.exists(path) && Files.isReadable(path) && filePath.toLowerCase().endsWith(".xlsx");
    }

    /**
     * Localiza a tabela de dados de vento e analisa as suas linhas de forma independente.
     *
     * @param filePath workbook XLSX legivel
     * @return registos analisados e erros especificos por linha
     */
    @Override
    public WeatherImportPayload execute(String filePath) {
        WeatherImportPayload payload = new WeatherImportPayload();
        Path path = Path.of(filePath);

        try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
            DataFormatter formatter = new DataFormatter();
            boolean headerFound = false;
            Map<String, Integer> columnIndex = new HashMap<>();
            Sheet dataSheet = null;
            int headerRowIndex = -1;

            for (int i = 0; i < workbook.getNumberOfSheets() && !headerFound; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }
                    Map<String, Integer> maybeHeader = detectHeader(row, formatter);
                    if (!maybeHeader.isEmpty()) {
                        columnIndex = maybeHeader;
                        headerFound = true;
                        dataSheet = sheet;
                        headerRowIndex = rowIndex;
                        break;
                    }
                }
            }

            if (!headerFound || dataSheet == null) {
                payload.addError("No supported header found in XLSX file.");
                return payload;
            }

            for (int rowIndex = headerRowIndex + 1; rowIndex <= dataSheet.getLastRowNum(); rowIndex++) {
                Row row = dataSheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row, columnIndex, formatter)) {
                    continue;
                }

                try {
                    WeatherDataDTO dto = parseWeatherRow(row, columnIndex, formatter, rowIndex + 1);
                    payload.addRecord(dto);
                } catch (IllegalArgumentException e) {
                    payload.addError("Line " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            payload.addError("Failed to read file: " + e.getMessage());
        }

        return payload;
    }

    private Map<String, Integer> detectHeader(Row row, DataFormatter formatter) {
        Map<String, Integer> columns = new HashMap<>();
        short lastCell = row.getLastCellNum();
        for (int i = 0; i < lastCell; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                continue;
            }
            String value = normalizeHeader(formatter.formatCellValue(cell));
            if (!value.isEmpty()) {
                columns.put(value, i);
            }
        }
        if (columns.containsKey(COL_ACA)
                && columns.containsKey(COL_ALT_INF)
                && columns.containsKey(COL_ALT_SUP)
                && columns.containsKey(COL_DIRECTION)
                && columns.containsKey(COL_VALUE)
                && columns.containsKey(COL_DAY)
                && columns.containsKey(COL_START)
                && columns.containsKey(COL_END)) {
            return columns;
        }
        return Map.of();
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replace(" ", "").replace(".", "").trim();
    }

    private boolean isRowEmpty(Row row, Map<String, Integer> columnIndex, DataFormatter formatter) {
        for (int index : columnIndex.values()) {
            Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private WeatherDataDTO parseWeatherRow(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, int lineNumber) {
        String areaId = readString(row, columnIndex.get(COL_ACA), formatter);
        if (areaId.isEmpty()) {
            throw new IllegalArgumentException("ACA identifier is required.");
        }

        double altInfFt = readDouble(row, columnIndex.get(COL_ALT_INF), formatter, "Alt_inf (ft)");
        double altSupFt = readDouble(row, columnIndex.get(COL_ALT_SUP), formatter, "Alt_Sup (ft)");
        double direction = readDouble(row, columnIndex.get(COL_DIRECTION), formatter, "Direction (degree)");
        double windSpeed = readDouble(row, columnIndex.get(COL_VALUE), formatter, "Value (knot)");

        LocalDate date = readDate(row, columnIndex.get(COL_DAY), formatter);
        LocalTime startTime = readTime(row, columnIndex.get(COL_START), formatter);
        LocalTime endTime = readTime(row, columnIndex.get(COL_END), formatter);
        if (startTime == null && endTime == null) {
            throw new IllegalArgumentException("Invalid time range.");
        }
        if (startTime == null) {
            startTime = endTime;
        }
        if (endTime == null) {
            endTime = startTime;
        }

        double altitudeMeters = ((altInfFt + altSupFt) / 2.0) * FEET_TO_METERS;

        return new WeatherDataDTO(
                areaId,
                date,
                startTime,
                endTime,
                windSpeed,
                direction,
                DEFAULT_TEMPERATURE_C,
                DEFAULT_PRESSURE_HPA,
                DEFAULT_HUMIDITY_PCT,
                altitudeMeters,
                lineNumber
        );
    }

    private String readString(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private double readDouble(Row row, int index, DataFormatter formatter, String label) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new IllegalArgumentException("Missing value for " + label + ".");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String value = formatter.formatCellValue(cell).trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + label + ": '" + value + "'.");
        }
    }

    private LocalDate readDate(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new IllegalArgumentException("Missing date value.");
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
            return dateTime.toLocalDate();
        }
        String value = formatter.formatCellValue(cell).trim();
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: '" + value + "'.");
        }
    }

    private LocalTime readTime(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return WeatherTimeFormatter.normalize(cell.getLocalDateTimeCellValue().toLocalTime());
        }
        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
