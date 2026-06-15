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
        ensureAuthorized();
        return service.listDraftFlightPlans(currentUsername(), isAdmin());
    }

    public TestFlightPlanResultDTO testFlightPlan(final String flightPlanId) throws IOException {
        ensureAuthorized();
        return service.testFlightPlan(flightPlanId, currentUsername(), isAdmin());
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
