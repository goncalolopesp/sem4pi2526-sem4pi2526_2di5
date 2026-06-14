package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.flightplan.CreateFlightPlanFromFileService;
import pt.isep.sem4pi.application.service.flightplan.CreateFlightPlanService;
import pt.isep.sem4pi.application.service.flightplan.ImportFlightPlanService;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

@UseCaseController
public class ImportFlightPlanController {

    private final AuthorizationService authz;
    private final ImportFlightPlanService service;

    public ImportFlightPlanController() {
        this.authz = AuthzRegistry.authorizationService();
        final var repos = PersistenceContext.repositories();
        final CreateFlightPlanService createFlightPlanService = new CreateFlightPlanService(
                repos.flightPlans(),
                repos.flightRoutes(),
                repos.airTransportCompanies(),
                repos.pilots(),
                repos.flights()
        );
        this.service = new ImportFlightPlanService(createFlightPlanService);
    }

    public ImportFlightPlanController(final AuthorizationService authz,
                                      final ImportFlightPlanService service) {
        this.authz = authz;
        this.service = service;
    }

    public FlightPlanImportResultDTO importFlightPlan(final String filePath) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        return new CreateFlightPlanFromFileService(service).createFromFile(filePath);
    }
}
