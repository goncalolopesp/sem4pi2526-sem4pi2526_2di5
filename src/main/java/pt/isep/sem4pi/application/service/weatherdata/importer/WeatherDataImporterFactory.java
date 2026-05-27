package pt.isep.sem4pi.application.service.weatherdata.importer;

public class WeatherDataImporterFactory {

    public WeatherDataImporter createImporter(String fileType, String filePath) {
        String normalizedType = normalizeType(fileType, filePath);
        if ("csv".equalsIgnoreCase(normalizedType)) {
            return new WeatherDataCSVImporter();
        }
        if ("xlsx".equalsIgnoreCase(normalizedType)) {
            return new WeatherDataXLSXImporter();
        }
        throw new IllegalArgumentException("Unsupported weather data file type: " + normalizedType);
    }

    private String normalizeType(String fileType, String filePath) {
        if (fileType != null && !fileType.trim().isEmpty()) {
            return fileType.trim().toLowerCase();
        }
        if (filePath == null) {
            return "";
        }
        int idx = filePath.lastIndexOf('.');
        if (idx < 0 || idx == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(idx + 1).toLowerCase();
    }
}
