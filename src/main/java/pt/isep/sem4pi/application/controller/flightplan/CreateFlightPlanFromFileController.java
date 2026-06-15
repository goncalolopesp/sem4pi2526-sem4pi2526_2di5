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
public class CreateFlightPlanFromFileController {

    private final AuthorizationService authz;
    private final CreateFlightPlanFromFileService service;

    public CreateFlightPlanFromFileController() {
        this.authz = AuthzRegistry.authorizationService();
        final var repos = PersistenceContext.repositories();
        final CreateFlightPlanService createFlightPlanService = new CreateFlightPlanService(
                repos.flightPlans(),
                repos.flightRoutes(),
                repos.airTransportCompanies(),
                repos.pilots(),
                repos.flights()
        );
        final ImportFlightPlanService importFlightPlanService = new ImportFlightPlanService(createFlightPlanService);
        this.service = new CreateFlightPlanFromFileService(importFlightPlanService);
    }

    public CreateFlightPlanFromFileController(final AuthorizationService authz,
                                              final CreateFlightPlanFromFileService service) {
        this.authz = authz;
        this.service = service;
    }

    public FlightPlanImportResultDTO validateFile(final String filePath) {
        ensureAuthorized();
        return service.validateFile(filePath);
    }

    public FlightPlanImportResultDTO createFromFile(final String filePath) {
        ensureAuthorized();
        return service.createFromFile(filePath);
    }

    private void ensureAuthorized() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
    }
}
