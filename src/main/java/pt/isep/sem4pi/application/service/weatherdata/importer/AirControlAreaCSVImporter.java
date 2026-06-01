package pt.isep.sem4pi.application.service.weatherdata.importer;

import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AirControlAreaCSVImporter {

    public Map<String, AirControlAreaBoundary> loadAreas(String filePath) {
        Map<String, AirControlAreaBoundary> areas = new HashMap<>();
        if (filePath == null || filePath.trim().isEmpty()) {
            return areas;
        }

        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            return areas;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean headerFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (!headerFound) {
                    if (isHeader(parts)) {
                        headerFound = true;
                    }
                    continue;
                }

                if (parts.length < 5) {
                    continue;
                }

                String areaId = parts[0].trim();
                if (areaId.isEmpty()) {
                    continue;
                }

                Double lat1 = parseDouble(parts[1]);
                Double lon1 = parseDouble(parts[2]);
                Double lat2 = parseDouble(parts[3]);
                Double lon2 = parseDouble(parts[4]);
                if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                    continue;
                }

                double minLat = Math.min(lat1, lat2);
                double maxLat = Math.max(lat1, lat2);
                double minLon = Math.min(lon1, lon2);
                double maxLon = Math.max(lon1, lon2);

                try {
                    AirControlAreaBoundary boundary = new AirControlAreaBoundary(minLat, maxLat, minLon, maxLon);
                    areas.put(areaId, boundary);
                } catch (IllegalArgumentException ignored) {
                    // Skip invalid boundary.
                }
            }
        } catch (IOException ignored) {
            return areas;
        }

        return areas;
    }

    private boolean isHeader(String[] parts) {
        if (parts.length < 5) {
            return false;
        }
        String first = parts[0].toLowerCase().trim();
        return first.equals("id") || first.contains("air control area");
    }

    private Double parseDouble(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

