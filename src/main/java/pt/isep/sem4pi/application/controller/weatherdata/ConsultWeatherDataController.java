package pt.isep.sem4pi.application.controller.weatherdata;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.weatherdata.ConsultWeatherDataService;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Application controller for US043, weather-data consultation by area and day.
 *
 * <p>It enforces the four roles allowed by the requirement and delegates the
 * query to a service so the presentation layer never accesses repositories
 * directly.</p>
 */
@UseCaseController
public class ConsultWeatherDataController {

    private final AuthorizationService authz;
    private final ConsultWeatherDataService service;

    public ConsultWeatherDataController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().weatherBatches());
    }

    public ConsultWeatherDataController(AuthorizationService authz, WeatherBatchRepository repository) {
        this.authz = authz;
        this.service = new ConsultWeatherDataService(repository);
    }

    /**
     * Returns weather measurements for one Air Control Area and calendar day.
     *
     * @param areaId Air Control Area identifier
     * @param date requested day
     * @return matching weather records, or an empty list when none exist
     */
    public List<WeatherDataViewDTO> consultByAreaAndDate(String areaId, LocalDate date) {
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.ADMIN,
                SEM4PIRoles.WEATHER_PERSON,
                SEM4PIRoles.PILOT,
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR
        );
        return service.findByAreaAndDate(areaId, date);
    }
}

