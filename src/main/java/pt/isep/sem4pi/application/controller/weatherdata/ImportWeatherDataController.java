package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.WeatherDataImportService;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

/**
 * Application controller for US042, the bulk import of weather data.
 *
 * <p>The controller is intentionally small: it protects the use case with the
 * required roles and delegates all parsing and persistence decisions to
 * {@link WeatherDataImportService}.</p>
 */
public class ImportWeatherDataController {
    private final WeatherDataImportService service;
    private final eapli.framework.infrastructure.authz.application.AuthorizationService authz;

    public ImportWeatherDataController() {
        this(new WeatherDataImportService(
                PersistenceContext.repositories().weatherBatches(),
                PersistenceContext.repositories().airControlAreas(),
                new WeatherDataImporterFactory()
        ), eapli.framework.infrastructure.authz.application.AuthzRegistry.authorizationService());
    }

    public ImportWeatherDataController(WeatherDataImportService service,
                                       eapli.framework.infrastructure.authz.application.AuthorizationService authz) {
        this.service = service;
        this.authz = authz;
    }

    /**
     * Imports weather records from a CSV or XLSX source.
     *
     * @param filePath path or known weather-data file name
     * @param fileType explicit file type; when omitted, the extension is used
     * @return summary containing imported/rejected totals and row errors
     * @throws IllegalArgumentException when the path or format is unsupported
     */
    public WeatherImportResultDTO importWeatherData(String filePath, String fileType) {
        authz.ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
        return service.importWeatherData(filePath, fileType);
    }
}
