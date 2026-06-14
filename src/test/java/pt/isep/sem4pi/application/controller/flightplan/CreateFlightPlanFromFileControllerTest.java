package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.application.service.flightplan.CreateFlightPlanFromFileService;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateFlightPlanFromFileControllerTest {

    @Test
    public void createFromFileAuthorizesPilotRole() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final CreateFlightPlanFromFileService service = mock(CreateFlightPlanFromFileService.class);
        final CreateFlightPlanFromFileController controller = new CreateFlightPlanFromFileController(authz, service);

        when(service.createFromFile("plan.dsldig5"))
                .thenReturn(FlightPlanImportResultDTO.success("FP-000001"));

        controller.createFromFile("plan.dsldig5");

        verify(authz).ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        verify(service).createFromFile("plan.dsldig5");
    }

    @Test
    public void validateFileAuthorizesPilotRole() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final CreateFlightPlanFromFileService service = mock(CreateFlightPlanFromFileService.class);
        final CreateFlightPlanFromFileController controller = new CreateFlightPlanFromFileController(authz, service);

        when(service.validateFile("plan.dsldig5"))
                .thenReturn(FlightPlanImportResultDTO.success(null));

        controller.validateFile("plan.dsldig5");

        verify(authz).ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
        verify(service).validateFile("plan.dsldig5");
    }
}
