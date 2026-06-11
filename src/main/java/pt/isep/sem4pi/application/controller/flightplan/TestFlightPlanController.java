package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.flightplan.TestFlightPlanService;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.simulation.CFlightTestRunner;

import java.io.IOException;
import java.util.List;

@UseCaseController
public class TestFlightPlanController {

    private final AuthorizationService authz;
    private final TestFlightPlanService service;

    public TestFlightPlanController() {
        this.authz = AuthzRegistry.authorizationService();
        final var repos = PersistenceContext.repositories();
        this.service = new TestFlightPlanService(
                repos.flightPlans(),
                repos.flightRoutes(),
                repos.airports(),
                repos.airControlAreas(),
                new CFlightTestRunner()
        );
    }

    public TestFlightPlanController(final AuthorizationService authz,
                                    final TestFlightPlanService service) {
        this.authz = authz;
        this.service = service;
    }

    public List<FlightPlan> listDraftFlightPlans() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.listDraftFlightPlans();
    }

    public TestFlightPlanResultDTO testFlightPlan(final String flightPlanId) throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return service.testFlightPlan(flightPlanId);
    }
}
