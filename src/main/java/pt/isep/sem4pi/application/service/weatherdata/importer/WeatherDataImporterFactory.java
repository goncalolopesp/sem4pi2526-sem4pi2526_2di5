package pt.isep.sem4pi.application.service.weatherdata.importer;

/**
 * Seleciona o parser da US042 de acordo com o tipo explicito ou a extensao do ficheiro.
 */
public class WeatherDataImporterFactory {

    /**
     * Cria um importador sem estado para o formato meteorologico pedido.
     *
     * @param fileType formato explicito, como {@code csv} ou {@code xlsx}
     * @param filePath caminho usado para inferir o formato quando {@code fileType} esta vazio
     * @return importador correspondente ao formato normalizado
     * @throws IllegalArgumentException quando nao e possivel determinar um formato suportado
     */
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
