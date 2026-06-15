package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.WeatherDataImportService;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

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

    public WeatherImportResultDTO importWeatherData(String filePath, String fileType) {
        authz.ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
        return service.importWeatherData(filePath, fileType);
    }
}
