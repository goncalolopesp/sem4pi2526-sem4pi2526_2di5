package pt.isep.sem4pi.application.service.weatherdata.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Extrai definicoes de Air Control Area embebidas num workbook XLSX da US042.
 *
 * <p>Todas as folhas sao pesquisadas ate encontrar um cabecalho ACA. Os pares
 * de coordenadas sao normalizados para latitude e longitude minima/maxima
 * antes de construir o limite de dominio.</p>
 */
public class AirControlAreaXLSXImporter {

    /**
     * Carrega todos os limites ACA validos encontrados no workbook.
     *
     * @param filePath caminho do workbook XLSX
     * @return limites indexados pelo identificador da area
     */
    public Map<String, AirControlAreaBoundary> loadAreas(String filePath) {
        Map<String, AirControlAreaBoundary> areas = new HashMap<>();
        if (filePath == null || filePath.trim().isEmpty()) {
            return areas;
        }

        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            return areas;
        }

        try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
            DataFormatter formatter = new DataFormatter();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                int headerRowIndex = findHeaderRow(sheet, formatter);
                if (headerRowIndex < 0) {
                    continue;
                }
                Map<String, Integer> header = mapHeader(sheet.getRow(headerRowIndex), formatter);
                if (!isAcaHeader(header)) {
                    continue;
                }
                for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }
                    String areaId = readString(row, header.get("id"), formatter);
                    if (areaId.isEmpty()) {
                        continue;
                    }
                    Double lat1 = readDouble(row, header.get("lat1"), formatter);
                    Double lon1 = readDouble(row, header.get("lon1"), formatter);
                    Double lat2 = readDouble(row, header.get("lat2"), formatter);
                    Double lon2 = readDouble(row, header.get("lon2"), formatter);
                    if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                        continue;
                    }
                    double minLat = Math.min(lat1, lat2);
                    double maxLat = Math.max(lat1, lat2);
                    double minLon = Math.min(lon1, lon2);
                    double maxLon = Math.max(lon1, lon2);
                    try {
                        areas.put(areaId, new AirControlAreaBoundary(minLat, maxLat, minLon, maxLon));
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid boundary.
                    }
                }
            }
        } catch (IOException ignored) {
            return areas;
        }

        return areas;
    }

    private int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Map<String, Integer> header = mapHeader(row, formatter);
            if (isAcaHeader(header)) {
                return rowIndex;
            }
        }
        return -1;
    }

    private Map<String, Integer> mapHeader(Row row, DataFormatter formatter) {
        Map<String, Integer> header = new HashMap<>();
        int latCount = 0;
        int lonCount = 0;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                continue;
            }
            String value = normalize(formatter.formatCellValue(cell));
            if (value.equals("id")) {
                header.put("id", i);
            } else if (value.startsWith("lat")) {
                latCount++;
                header.put("lat" + latCount, i);
            } else if (value.startsWith("long")) {
                lonCount++;
                header.put("lon" + lonCount, i);
            }
        }
        return header;
    }

    private boolean isAcaHeader(Map<String, Integer> header) {
        return header.containsKey("id")
                && header.containsKey("lat1")
                && header.containsKey("lat2")
                && header.containsKey("lon1")
                && header.containsKey("lon2");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replace(".", "").replace(" ", "").trim();
    }

    private String readString(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private Double readDouble(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

