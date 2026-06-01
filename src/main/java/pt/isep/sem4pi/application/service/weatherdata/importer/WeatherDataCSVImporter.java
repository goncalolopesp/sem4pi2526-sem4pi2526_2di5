package pt.isep.sem4pi.application.service.weatherdata.importer;

import pt.isep.sem4pi.domain.weatherdata.DTO.WeatherDataDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WeatherDataCSVImporter implements WeatherDataImporter {

    private static final double DEFAULT_TEMPERATURE_C = 15.0;
    private static final double DEFAULT_PRESSURE_HPA = 1013.25;
    private static final double DEFAULT_HUMIDITY_PCT = 50.0;
    private static final double FEET_TO_METERS = 0.3048;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public boolean validate(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        Path path = Path.of(filePath);
        return Files.exists(path) && Files.isReadable(path) && filePath.toLowerCase().endsWith(".csv");
    }

    @Override
    public WeatherImportPayload execute(String filePath) {
        WeatherImportPayload payload = new WeatherImportPayload();
        Path path = Path.of(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            boolean headerValidated = false;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (!headerValidated) {
                    if (looksLikeHeader(line)) {
                        if (!isSupportedWindHeader(line)) {
                            payload.addError("Line " + lineNumber + ": Unsupported CSV layout. Expected wind data file header.");
                            return payload;
                        }
                        headerValidated = true;
                        continue;
                    }
                    headerValidated = true;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 12) {
                    payload.addError("Line " + lineNumber + ": Expected at least 12 columns but found " + parts.length + ".");
                    continue;
                }

                try {
                    WeatherDataDTO dto = parseWeatherData(parts, lineNumber);
                    payload.addRecord(dto);
                } catch (IllegalArgumentException e) {
                    payload.addError("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            payload.addError("Failed to read file: " + e.getMessage());
        }

        return payload;
    }

    private boolean looksLikeHeader(String line) {
        String lower = line.toLowerCase();
        return lower.contains("aca") || lower.contains("air control area") || lower.contains("alt_inf");
    }

    private boolean isSupportedWindHeader(String line) {
        String lower = line.toLowerCase();
        return lower.contains("alt_inf") && lower.contains("direction") && lower.contains("value");
    }

    private WeatherDataDTO parseWeatherData(String[] parts, int lineNumber) {
        String areaId = parts[0].trim();
        if (areaId.isEmpty()) {
            throw new IllegalArgumentException("ACA identifier is required.");
        }

        double altInfFt = parseDouble(parts[5], "Alt_inf (ft)");
        double altSupFt = parseDouble(parts[6], "Alt_Sup (ft)");
        double direction = parseDouble(parts[7], "Direction (degree)");
        double windSpeed = parseDouble(parts[8], "Value (knot)");

        LocalDate date = parseDate(parts[9]);
        LocalTime startTime = parseTimeValue(parts[10]);
        LocalTime endTime = parseTimeValue(parts[11]);
        if (startTime == null && endTime == null) {
            throw new IllegalArgumentException("Invalid time range: '" + parts[10] + "' - '" + parts[11] + "'.");
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

    private double parseDouble(String value, String label) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + label + ": '" + value + "'.");
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: '" + value + "'.");
        }
    }

    private LocalTime parseTimeValue(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(trimmed, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
