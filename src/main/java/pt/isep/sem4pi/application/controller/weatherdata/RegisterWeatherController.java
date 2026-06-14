package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.RegisterWeatherService;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegisterWeatherController {
    private final RegisterWeatherService service;
    private final eapli.framework.infrastructure.authz.application.AuthorizationService authz;

    public RegisterWeatherController() {
        this(PersistenceContext.repositories().weatherBatches(),
                eapli.framework.infrastructure.authz.application.AuthzRegistry.authorizationService());
    }

    public RegisterWeatherController(WeatherBatchRepository repository,
                                     eapli.framework.infrastructure.authz.application.AuthorizationService authz) {
        this.service = new RegisterWeatherService(repository);
        this.authz = authz;
    }

    public WeatherImportBatch registerWeather(String areaId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                              double temp, double windSpeed, double windDir, double humidity,
                                              double pressure, double altitude) {
        authz.ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
        return service.registerManualData(
                areaId, date, startTime, endTime, temp, windSpeed, windDir, humidity, pressure, altitude);
    }
}