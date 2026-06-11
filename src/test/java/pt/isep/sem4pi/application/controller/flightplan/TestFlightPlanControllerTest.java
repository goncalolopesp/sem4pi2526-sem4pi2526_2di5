package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.flightplan.TestFlightPlanService;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightTestExecutionResultDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestFlightPlanControllerTest {

    private AuthorizationService mockAuthz;
    private TestFlightPlanService mockService;
    private TestFlightPlanController controller;

    @Before
    public void setUp() {
        mockAuthz = Mockito.mock(AuthorizationService.class);
        mockService = Mockito.mock(TestFlightPlanService.class);
        controller = new TestFlightPlanController(mockAuthz, mockService);
    }

    @Test
    public void ensureListDraftFlightPlansChecksAuthorizationAndDelegates() {
        final List<FlightPlan> plans = List.of(Mockito.mock(FlightPlan.class));
        when(mockService.listDraftFlightPlans()).thenReturn(plans);

        final List<FlightPlan> result = controller.listDraftFlightPlans();

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
        verify(mockService, times(1)).listDraftFlightPlans();
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
        when(mockService.testFlightPlan(anyString())).thenReturn(dto);

        final TestFlightPlanResultDTO result = controller.testFlightPlan("FP-001");

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
        verify(mockService, times(1)).testFlightPlan(anyString());
        assertEquals(dto, result);
    }

    @Test(expected = RuntimeException.class)
    public void ensureTestFlightPlanFailsWhenNotAuthorized() throws Exception {
        doThrow(new RuntimeException("Not authorized"))
                .when(mockAuthz).ensureAuthenticatedUserHasAnyOf(any());

        controller.testFlightPlan("FP-001");
    }
}
