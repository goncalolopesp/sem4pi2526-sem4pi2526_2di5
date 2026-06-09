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

    public List<FlightPlan> myFlightPlans() {
        ensureAuthorized();
        return service.listFlightPlansAccessibleBy(currentUsername(), isAdmin());
    }

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
