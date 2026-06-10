package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.flightplan.CreateFlightPlanService;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.List;

@UseCaseController
public class CreateFlightPlanController {

    private final AuthorizationService authz;
    private final CreateFlightPlanService service;

    public CreateFlightPlanController() {
        this.authz = AuthzRegistry.authorizationService();
        final var repos = PersistenceContext.repositories();
        this.service = new CreateFlightPlanService(
                repos.flightPlans(),
                repos.flightRoutes(),
                repos.airTransportCompanies(),
                repos.pilots(),
                repos.flights()
        );
    }

    public CreateFlightPlanController(final AuthorizationService authz,
                                      final CreateFlightPlanService service) {
        this.authz = authz;
        this.service = service;
    }

    public FlightPlan createFlightPlan(final CreateFlightPlanDTO dto) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.createFlightPlan(dto);
    }

    public Iterable<FlightRoute> getRoutes() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.listRoutes();
    }

    public List<Pilot> getPilotsForRoute(final String routeId) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.listPilotsForRoute(routeId);
    }

    public List<Aircraft> getAircraftForRoute(final String routeId) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.listAircraftForRoute(routeId);
    }

    public List<Flight> getFlights() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.listFlights();
    }

    public List<FlightPlan> listFlightPlans() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.listFlightPlans();
    }
}
