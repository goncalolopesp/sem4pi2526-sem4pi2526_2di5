package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.flightplan.AssociateWeatherDataService;
import pt.isep.sem4pi.application.service.flightplan.WeatherAssociationResult;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Application controller for US082, associating existing weather data with a
 * flight plan.
 *
 * <p>It obtains the authenticated username and whether the user is an
 * administrator, then passes those facts to the service for ownership
 * validation. Pilots can manage only their plans; administrators can manage
 * all plans.</p>
 */
@UseCaseController
public class AssociateWeatherDataController {

    private final AuthorizationService authz;
    private final AssociateWeatherDataService service;

    public AssociateWeatherDataController() {
        this.authz = AuthzRegistry.authorizationService();
        final var repositories = PersistenceContext.repositories();
        this.service = new AssociateWeatherDataService(
                repositories.flightPlans(), repositories.weatherBatches());
    }

    public AssociateWeatherDataController(AuthorizationService authz,
                                          AssociateWeatherDataService service) {
        this.authz = authz;
        this.service = service;
    }

    /**
     * Lists flight plans visible to the authenticated pilot or administrator.
     *
     * @return plans owned by the pilot, or every plan for an administrator
     */
    public List<FlightPlan> myFlightPlans() {
        ensureAuthorized();
        return service.listFlightPlansAccessibleBy(currentUsername(), isAdmin());
    }

    /**
     * Lists persisted weather records available for selection.
     *
     * @return sorted view DTOs with identifiers required for association
     */
    public List<WeatherDataViewDTO> weatherData() {
        ensureAuthorized();
        return service.listWeatherData();
    }

    public WeatherAssociationResult associate(String flightPlanId, String areaId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime) {
        ensureAuthorized();
        return service.associate(
                flightPlanId, areaId, date, startTime, endTime, currentUsername(), isAdmin());
    }

    /**
     * Associates multiple selected weather records in one operation.
     *
     * @param flightPlanId target flight-plan identity
     * @param weatherDataIds persisted weather-data identities
     * @return updated plan, number of additions and test-invalidation state
     */
    public WeatherAssociationResult associate(String flightPlanId, List<Long> weatherDataIds) {
        ensureAuthorized();
        return service.associateByIds(
                flightPlanId, weatherDataIds, currentUsername(), isAdmin());
    }

    private void ensureAuthorized() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
    }

    private boolean isAdmin() {
        return authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN);
    }

    private String currentUsername() {
        return authz.session()
                .orElseThrow(() -> new IllegalStateException("An authenticated session is required."))
                .authenticatedUser().identity().toString();
    }
}
