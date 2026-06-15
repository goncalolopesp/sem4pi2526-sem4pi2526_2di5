package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.flightplan.TestFlightPlanService;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightTestExecutionResultDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestFlightPlanControllerTest {

    private TestFlightPlanService mockService;
    private TestFlightPlanController controller;

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory",
                "pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory");
        PersistenceContext.resetForTesting();
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder()
        );

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        final SystemUser pilotUser = new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder())
                .withUsername("pilot")
                .withPassword("Password1")
                .withName("Pilot", "User")
                .withEmail("pilot@tap.pt")
                .withRoles(SEM4PIRoles.PILOT)
                .build();
        PersistenceContext.repositories().users().save(pilotUser);
        AuthzRegistry.authenticationService().authenticate("pilot", "Password1");

        mockService = mock(TestFlightPlanService.class);
        controller = new TestFlightPlanController(AuthzRegistry.authorizationService(), mockService);
    }

    @Test
    public void ensureListDraftFlightPlansChecksAuthorizationAndDelegates() {
        final List<FlightPlan> plans = List.of(Mockito.mock(FlightPlan.class));
        when(mockService.listDraftFlightPlans(anyString(), anyBoolean())).thenReturn(plans);

        final List<FlightPlan> result = controller.listDraftFlightPlans();

        verify(mockService, times(1)).listDraftFlightPlans(eq("pilot"), eq(false));
        assertEquals(plans, result);
    }

    @Test
    public void ensureTestFlightPlanChecksAuthorizationAndDelegates() throws Exception {
        final TestFlightPlanResultDTO dto = new TestFlightPlanResultDTO(
                "FP-001",
                new US120ValidationResult(List.of(), List.of(), null),
                new FlightTestExecutionResultDTO(true, true, "report.txt", "ok"),
                true,
                "validated"
        );
        when(mockService.testFlightPlan(anyString(), anyString(), anyBoolean())).thenReturn(dto);

        final TestFlightPlanResultDTO result = controller.testFlightPlan("FP-001");

        verify(mockService, times(1)).testFlightPlan("FP-001", "pilot", false);
        assertEquals(dto, result);
    }

    @Test(expected = RuntimeException.class)
    public void ensureTestFlightPlanFailsWhenNotAuthorized() throws Exception {
        AuthzRegistry.authorizationService().clearSession();
        controller.testFlightPlan("FP-001");
    }
}
