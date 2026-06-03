package pt.isep.sem4pi.application.controller.route;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.route.DeactivateFlightRouteService;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for DeactivateFlightRouteController.
 * 
 * Tests cover:
 * - Delegation to service layer for deactivateRoute
 * - Correct parameters passed to service
 * - Exception propagation (IllegalArgumentException, IllegalStateException, RuntimeException)
 * - Repository delegation for getRoutes
 * - Empty and multiple routes handling
 * - Repository exception propagation
 * - Validation delegation for validateDeactivation
 * 
 * @author SEM4PI Team
 */
public class DeactivateFlightRouteControllerTest {

    /** Mock flight route deactivation service */
    private DeactivateFlightRouteService mockService;
    
    /** Mock flight route repository */
    private FlightRouteRepository mockRouteRepo;
    
    /** Controller being tested */
    private DeactivateFlightRouteController controller;

    /**
     * Sets up test fixtures before each test.
     */
    @Before
    public void setUp() {
        mockService = Mockito.mock(DeactivateFlightRouteService.class);
        mockRouteRepo = Mockito.mock(FlightRouteRepository.class);
        controller = new DeactivateFlightRouteController(mockService, mockRouteRepo);
    }

    // ========================
    // deactivateRoute
    // ========================

    /**
     * Test that the controller delegates deactivateRoute to the service.
     */
    @Test
    public void ensureDeactivateRouteDelegatesToService() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        LocalDate date = LocalDate.of(2026, 7, 1);

        when(mockService.deactivateRoute("TP12", date)).thenReturn(mockRoute);

        FlightRoute result = controller.deactivateRoute("TP12", date);

        verify(mockService, times(1)).deactivateRoute("TP12", date);
        assertEquals(mockRoute, result);
    }

    @Test
    public void ensureDeactivateRouteReturnsFlightRoute() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        LocalDate date = LocalDate.of(2026, 7, 1);

        when(mockService.deactivateRoute("TP12", date)).thenReturn(mockRoute);

        FlightRoute result = controller.deactivateRoute("TP12", date);

        assertNotNull(result);
    }

    @Test
    public void ensureDeactivateRoutePassesCorrectRouteId() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        LocalDate date = LocalDate.of(2026, 7, 1);

        when(mockService.deactivateRoute("AB99", date)).thenReturn(mockRoute);

        controller.deactivateRoute("AB99", date);

        verify(mockService).deactivateRoute(eq("AB99"), eq(date));
    }

    @Test
    public void ensureDeactivateRoutePassesCorrectDate() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        LocalDate date = LocalDate.of(2027, 12, 31);

        when(mockService.deactivateRoute("TP12", date)).thenReturn(mockRoute);

        controller.deactivateRoute("TP12", date);

        verify(mockService).deactivateRoute(eq("TP12"), eq(date));
    }

    // ========================
    // Service exception propagation
    // ========================

    @Test(expected = IllegalArgumentException.class)
    public void ensureIllegalArgumentExceptionFromServiceIsPropagated() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        when(mockService.deactivateRoute("TP12", date))
                .thenThrow(new IllegalArgumentException("Flight Route not found."));

        controller.deactivateRoute("TP12", date);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureIllegalStateExceptionFromServiceIsPropagated() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        when(mockService.deactivateRoute("TP12", date))
                .thenThrow(new IllegalStateException("Cannot deactivate: there are planned flights."));

        controller.deactivateRoute("TP12", date);
    }

    @Test(expected = RuntimeException.class)
    public void ensureRuntimeExceptionFromServiceIsPropagated() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        when(mockService.deactivateRoute("TP12", date))
                .thenThrow(new RuntimeException("Unexpected error"));

        controller.deactivateRoute("TP12", date);
    }

    // ========================
    // getRoutes
    // ========================

    @Test
    public void ensureGetRoutesDelegatesToRepository() {
        Iterable<FlightRoute> mockRoutes = Collections.emptyList();
        when(mockRouteRepo.findAll()).thenReturn(mockRoutes);

        Iterable<FlightRoute> result = controller.getRoutes();

        verify(mockRouteRepo, times(1)).findAll();
        assertEquals(mockRoutes, result);
    }

    @Test
    public void ensureGetRoutesReturnsEmptyWhenNoRoutes() {
        when(mockRouteRepo.findAll()).thenReturn(Collections.emptyList());

        Iterable<FlightRoute> result = controller.getRoutes();

        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void ensureGetRoutesReturnsAllRoutes() {
        FlightRoute route1 = Mockito.mock(FlightRoute.class);
        FlightRoute route2 = Mockito.mock(FlightRoute.class);
        List<FlightRoute> routes = Arrays.asList(route1, route2);

        when(mockRouteRepo.findAll()).thenReturn(routes);

        Iterable<FlightRoute> result = controller.getRoutes();

        int count = 0;
        for (FlightRoute ignored : result) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test(expected = RuntimeException.class)
    public void ensureGetRoutesRepositoryExceptionIsPropagated() {
        when(mockRouteRepo.findAll()).thenThrow(new RuntimeException("DB error"));

        controller.getRoutes();
    }

    // validateDeactivation
    @Test
    public void ensureValidateDeactivationDelegatesToService() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        doNothing().when(mockService).validateDeactivation("TP12", date);

        controller.validateDeactivation("TP12", date);

        verify(mockService, times(1)).validateDeactivation("TP12", date);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureValidateDeactivationPropagatesIllegalStateException() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        doThrow(new IllegalStateException("Cannot deactivate: there are planned flights."))
                .when(mockService).validateDeactivation("TP12", date);

        controller.validateDeactivation("TP12", date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureValidateDeactivationPropagatesIllegalArgumentException() {
        LocalDate date = LocalDate.of(2026, 7, 1);

        doThrow(new IllegalArgumentException("Flight Route not found."))
                .when(mockService).validateDeactivation("UNKNOWN", date);

        controller.validateDeactivation("UNKNOWN", date);
    }
}
