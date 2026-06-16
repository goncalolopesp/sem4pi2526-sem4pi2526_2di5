package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.WeatherDataImportService;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

/**
 * Controller aplicacional da US042, responsavel pela importacao em massa de dados meteorologicos.
 *
 * <p>Este controller e intencionalmente simples: protege o caso de uso com os
 * papeis exigidos e delega todas as decisoes de parsing e persistencia para
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
     * Importa registos meteorologicos a partir de uma fonte CSV ou XLSX.
     *
     * @param filePath caminho ou nome de ficheiro conhecido de dados meteorologicos
     * @param fileType tipo de ficheiro explicito; quando omitido, usa-se a extensao
     * @return resumo com totais importados/rejeitados e erros por linha
     * @throws IllegalArgumentException quando o caminho ou formato nao e suportado
     */
    public WeatherImportResultDTO importWeatherData(String filePath, String fileType) {
        authz.ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
        return service.importWeatherData(filePath, fileType);
    }
}
