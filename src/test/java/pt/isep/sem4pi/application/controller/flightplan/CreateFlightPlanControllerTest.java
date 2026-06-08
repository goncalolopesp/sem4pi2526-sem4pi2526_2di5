package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.flightplan.CreateFlightPlanService;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.route.FlightRoute;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class CreateFlightPlanControllerTest {

    private AuthorizationService mockAuthz;
    private CreateFlightPlanService mockService;
    private CreateFlightPlanController controller;

    @Before
    public void setUp() {
        mockAuthz = Mockito.mock(AuthorizationService.class);
        mockService = Mockito.mock(CreateFlightPlanService.class);
        controller = new CreateFlightPlanController(mockAuthz, mockService);
    }

    @Test
    public void ensureCreateFlightPlanChecksAuthorizationAndDelegates() {
        final CreateFlightPlanDTO dto = new CreateFlightPlanDTO();
        final FlightPlan mockPlan = Mockito.mock(FlightPlan.class);
        when(mockService.createFlightPlan(dto)).thenReturn(mockPlan);

        final FlightPlan result = controller.createFlightPlan(dto);

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
        verify(mockService, times(1)).createFlightPlan(dto);
        assertEquals(mockPlan, result);
    }

    @Test
    public void ensureListFlightPlansChecksAuthorizationAndDelegates() {
        final List<FlightPlan> plans = List.of(Mockito.mock(FlightPlan.class));
        when(mockService.listFlightPlans()).thenReturn(plans);

        final List<FlightPlan> result = controller.listFlightPlans();

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
        verify(mockService, times(1)).listFlightPlans();
        assertEquals(plans, result);
    }

    @Test
    public void ensureGetFlightsChecksAuthorizationAndDelegates() {
        final List<Flight> flights = List.of(Mockito.mock(Flight.class));
        when(mockService.listFlights()).thenReturn(flights);

        final List<Flight> result = controller.getFlights();

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
        verify(mockService, times(1)).listFlights();
        assertEquals(flights, result);
    }

    @Test
    public void ensureGetRoutesChecksAuthorizationAndDelegates() {
        final Iterable<FlightRoute> routes = Collections.emptyList();
        when(mockService.listRoutes()).thenReturn(routes);

        final Iterable<FlightRoute> result = controller.getRoutes();

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
        verify(mockService, times(1)).listRoutes();
        assertNotNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void ensureCreateFlightPlanFailsWhenNotAuthorized() {
        doThrow(new RuntimeException("Not authorized"))
                .when(mockAuthz).ensureAuthenticatedUserHasAnyOf(any());

        controller.createFlightPlan(new CreateFlightPlanDTO());
    }

    @Test(expected = RuntimeException.class)
    public void ensureListFlightPlansFailsWhenNotAuthorized() {
        doThrow(new RuntimeException("Not authorized"))
                .when(mockAuthz).ensureAuthenticatedUserHasAnyOf(any());

        controller.listFlightPlans();
    }
}
