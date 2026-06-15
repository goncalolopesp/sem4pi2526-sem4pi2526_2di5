package pt.isep.sem4pi.application.service.weatherdata.importer;

/**
 * Strategy used by US042 to isolate each weather-file format.
 *
 * <p>Implementations only parse source data. Domain validation, area creation
 * and persistence remain responsibilities of the application service.</p>
 */
public interface WeatherDataImporter {
    /**
     * Checks whether the source exists, is readable and has the expected type.
     *
     * @param filePath source file path
     * @return {@code true} when this importer can read the source
     */
    boolean validate(String filePath);

    /**
     * Parses the complete source while preserving valid rows and row errors.
     *
     * @param filePath source file path
     * @return parsed records and errors
     */
    WeatherImportPayload execute(String filePath);
}

