package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class WeatherDataTotalsController {
    private final WeatherBatchRepository repository;
    private final eapli.framework.infrastructure.authz.application.AuthorizationService authz;

    public WeatherDataTotalsController() {
        this.repository = PersistenceContext.repositories().weatherBatches();
        this.authz = eapli.framework.infrastructure.authz.application.AuthzRegistry.authorizationService();
    }

    public long totalWeatherRecords() {
        authz.ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
        return repository.totalWeatherRecords();
    }
}
