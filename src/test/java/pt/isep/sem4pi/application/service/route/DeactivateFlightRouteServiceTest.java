package pt.isep.sem4pi.application.service.route;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for DeactivateFlightRouteService.
 *
 * Tests cover:
 * - Happy path: successful deactivation when no planned flights exist
 * - Service calls deactivate and save on repository
 * - Service checks for planned flights and enforces business rule
 * - Service prevents deactivation if flights exist after the date
 * - Route not found handling
 * - Invalid route ID handling
 * - Null parameters handling
 * - Repository exception propagation
 * - Validation method behavior
 * - Different date handling (future, today)
 *
 * @author SEM4PI Team
 */
public class DeactivateFlightRouteServiceTest {

    /** Mock repository for flight routes */
    private FlightRouteRepository mockRouteRepo;

    /** Mock repository for flights */
    private FlightRepository mockFlightRepo;

    /** Service being tested */
    private DeactivateFlightRouteService service;

    /**
     * Sets up test fixtures before each test.
     */
    @Before
    public void setUp() {
        mockRouteRepo = Mockito.mock(FlightRouteRepository.class);
        mockFlightRepo = Mockito.mock(FlightRepository.class);
        service = new DeactivateFlightRouteService(mockRouteRepo, mockFlightRepo);
    }

    /// Happy Path
    /**
     * Test that a route can be deactivated successfully when no planned flights exist.
     */
    @Test
    public void ensureDeactivateRouteSucceedsWhenNoPlannedFlightsExist() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        FlightRoute result = service.deactivateRoute("TP12", deactivationDate);

        assertNotNull(result);
        assertEquals(mockRoute, result);
    }

    @Test
    public void ensureDeactivateRouteCallsDeactivateOnDomainEntity() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        service.deactivateRoute("TP12", deactivationDate);

        verify(mockRoute, times(1)).deactivate(deactivationDate);
    }

    @Test
    public void ensureDeactivateRouteSavesToRepository() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        service.deactivateRoute("TP12", deactivationDate);

        verify(mockRouteRepo, times(1)).save(mockRoute);
    }

    @Test
    public void ensureDeactivateRouteChecksForPlannedFlights() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        service.deactivateRoute("TP12", deactivationDate);

        verify(mockFlightRepo, times(1)).hasFlightsForRouteAfterDate(routeId, deactivationDate);
    }

    @Test
    public void ensureDeactivateRouteLooksUpRouteInRepository() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        service.deactivateRoute("TP12", deactivationDate);

        verify(mockRouteRepo, times(1)).ofIdentity(routeId);
    }

    /// US074 Rule: Cannot deactivate if planned flights exist after date
    @Test(expected = IllegalStateException.class)
    public void ensureDeactivateRouteFailsWhenPlannedFlightsExistAfterDate() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(true);

        service.deactivateRoute("TP12", deactivationDate);
    }

    @Test
    public void ensureDeactivateRouteDoesNotSaveWhenPlannedFlightsExist() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(true);

        try {
            service.deactivateRoute("TP12", deactivationDate);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        verify(mockRouteRepo, never()).save(any());
    }

    @Test
    public void ensureDeactivateRouteDoesNotCallDeactivateWhenPlannedFlightsExist() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(true);

        try {
            service.deactivateRoute("TP12", deactivationDate);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        verify(mockRoute, never()).deactivate(any());
    }

    /// Route not found
    @Test(expected = IllegalArgumentException.class)
    public void ensureDeactivateRouteFailsWhenRouteNotFound() {
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.empty());

        service.deactivateRoute("TP12", deactivationDate);
    }

    @Test
    public void ensureDeactivateRouteDoesNotCheckFlightsWhenRouteNotFound() {
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.empty());

        try {
            service.deactivateRoute("TP12", deactivationDate);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        verify(mockFlightRepo, never()).hasFlightsForRouteAfterDate(any(), any());
    }

    /// Invalid route ID
    @Test(expected = IllegalArgumentException.class)
    public void ensureDeactivateRouteFailsWithInvalidRouteID() {
        service.deactivateRoute("INVALID!", LocalDate.of(2026, 7, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureDeactivateRouteFailsWithEmptyRouteID() {
        service.deactivateRoute("", LocalDate.of(2026, 7, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureDeactivateRouteFailsWithNullRouteID() {
        service.deactivateRoute(null, LocalDate.of(2026, 7, 1));
    }

    /// Repository failure propagation
    @Test(expected = RuntimeException.class)
    public void ensureRepositorySaveExceptionIsPropagated() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate deactivationDate = LocalDate.of(2026, 7, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, deactivationDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenThrow(new RuntimeException("DB error"));

        service.deactivateRoute("TP12", deactivationDate);
    }

    @Test(expected = RuntimeException.class)
    public void ensureRepositoryLookupExceptionIsPropagated() {
        FlightRouteID routeId = new FlightRouteID("TP12");

        when(mockRouteRepo.ofIdentity(routeId)).thenThrow(new RuntimeException("DB error"));

        service.deactivateRoute("TP12", LocalDate.of(2026, 7, 1));
    }

    /// Different dates
    @Test
    public void ensureDeactivateRouteWorksWithFutureDate() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate futureDate = LocalDate.of(2027, 1, 1);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, futureDate)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        FlightRoute result = service.deactivateRoute("TP12", futureDate);

        assertNotNull(result);
        verify(mockRoute).deactivate(futureDate);
    }

    @Test
    public void ensureDeactivateRouteWorksWithTodayDate() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate today = LocalDate.now();

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, today)).thenReturn(false);
        when(mockRouteRepo.save(mockRoute)).thenReturn(mockRoute);

        FlightRoute result = service.deactivateRoute("TP12", today);

        assertNotNull(result);
        verify(mockRoute).deactivate(today);
    }

    /// Validation Method Tests
    @Test
    public void ensureValidateDeactivationPassesWhenNoFlightsExist() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate date = LocalDate.now().plusDays(5);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, date)).thenReturn(false);

        service.validateDeactivation("TP12", date);

        verify(mockRouteRepo, never()).save(any());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureValidateDeactivationFailsWhenFlightsExist() {
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        FlightRouteID routeId = new FlightRouteID("TP12");
        LocalDate date = LocalDate.now().plusDays(5);

        when(mockRouteRepo.ofIdentity(routeId)).thenReturn(Optional.of(mockRoute));
        when(mockFlightRepo.hasFlightsForRouteAfterDate(routeId, date)).thenReturn(true);

        service.validateDeactivation("TP12", date);
    }
}
