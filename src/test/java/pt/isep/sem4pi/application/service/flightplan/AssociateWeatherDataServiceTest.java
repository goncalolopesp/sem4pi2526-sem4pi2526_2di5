package pt.isep.sem4pi.application.service.flightplan;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Service-level tests for US082, adding weather data to flight plans.
 *
 * <p>The service owns the cross-aggregate part of the use case: it resolves
 * flight plans and weather data, validates pilot ownership, allows admin
 * bypass through the {@code canManageAll} flag and persists the changed plan.</p>
 */
public class AssociateWeatherDataServiceTest {

    private static final String PLAN_ID = "FP-001";
    private static final String AREA_ID = "LPPC";
    private static final LocalDate DATE = LocalDate.of(2026, 6, 9);
    private static final LocalTime START = LocalTime.of(10, 0);
    private static final LocalTime END = LocalTime.of(11, 0);

    private FlightPlanRepository flightPlanRepository;
    private WeatherBatchRepository weatherRepository;
    private AssociateWeatherDataService service;
    private FlightPlan plan;
    private Pilot pilot;

    @Before
    public void setUp() {
        flightPlanRepository = mock(FlightPlanRepository.class);
        weatherRepository = mock(WeatherBatchRepository.class);
        service = new AssociateWeatherDataService(flightPlanRepository, weatherRepository);
        plan = mock(FlightPlan.class);
        pilot = mock(Pilot.class);
        when(plan.pilot()).thenReturn(pilot);
    }

    /**
     * Legacy single-record flow: existing weather is associated and the result
     * tells the UI when a previous test became invalid.
     */
    @Test
    public void ensureAssociationPersistsExistingWeatherAndReportsInvalidation() {
        final WeatherData weatherData = mock(WeatherData.class);
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherData(AREA_ID, DATE, START, END))
                .thenReturn(Optional.of(weatherData));
        when(plan.associateWeatherData(weatherData)).thenReturn(true);
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final WeatherAssociationResult result =
                service.associate(PLAN_ID, AREA_ID, DATE, START, END, "pilot");

        assertSame(plan, result.flightPlan());
        assertTrue(result.previousTestInvalidated());
        verify(plan).associateWeatherData(weatherData);
        verify(flightPlanRepository).save(plan);
    }

    /**
     * A pilot cannot change another pilot's flight plan; this is the ownership
     * protection required by US082.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensurePilotCannotUpdateAnotherPilotsFlightPlan() {
        when(pilot.isOwnedBy("other-pilot")).thenReturn(false);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));

        service.associate(PLAN_ID, AREA_ID, DATE, START, END, "other-pilot");
    }

    /**
     * Weather must already be persisted by US041/US042 before it can be linked
     * to a flight plan.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensureWeatherMustAlreadyExist() {
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherData(AREA_ID, DATE, START, END))
                .thenReturn(Optional.empty());

        service.associate(PLAN_ID, AREA_ID, DATE, START, END, "pilot");
    }

    /**
     * The list shown to a pilot contains only plans owned by that pilot.
     */
    @Test
    public void ensureOnlyOwnedFlightPlansAreListed() {
        final FlightPlan otherPlan = mock(FlightPlan.class);
        final Pilot otherPilot = mock(Pilot.class);
        when(otherPlan.pilot()).thenReturn(otherPilot);
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(otherPilot.isOwnedBy("pilot")).thenReturn(false);
        when(flightPlanRepository.findAll()).thenReturn(List.of(plan, otherPlan));

        final List<FlightPlan> result = service.listFlightPlansOwnedBy("pilot");

        assertEquals(List.of(plan), result);
    }

    /**
     * Main US082 batch scenario: several selected weather rows are associated
     * in one operation and the plan is saved once at the end.
     */
    @Test
    public void ensureSeveralWeatherRecordsCanBeAssociatedInOneOperation() {
        final WeatherData first = mock(WeatherData.class);
        final WeatherData second = mock(WeatherData.class);
        final List<WeatherData> associated = new java.util.ArrayList<>();
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherDataById(1L)).thenReturn(Optional.of(first));
        when(weatherRepository.findWeatherDataById(2L)).thenReturn(Optional.of(second));
        when(plan.weatherData()).thenAnswer(invocation -> List.copyOf(associated));
        when(plan.associateWeatherData(any())).thenAnswer(invocation -> {
            associated.add(invocation.getArgument(0));
            return associated.size() == 1;
        });
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final WeatherAssociationResult result =
                service.associateByIds(PLAN_ID, List.of(1L, 2L), "pilot", false);

        assertEquals(2, result.associatedWeatherCount());
        assertTrue(result.previousTestInvalidated());
        verify(plan).associateWeatherData(first);
        verify(plan).associateWeatherData(second);
        verify(flightPlanRepository, times(1)).save(plan);
    }

    /**
     * Repeated selected IDs are deduplicated before association, preventing the
     * same weather record from being processed multiple times.
     */
    @Test
    public void ensureRepeatedWeatherIdsAreProcessedOnlyOnce() {
        final WeatherData weatherData = mock(WeatherData.class);
        final List<WeatherData> associated = new java.util.ArrayList<>();
        when(pilot.isOwnedBy("pilot")).thenReturn(true);
        when(flightPlanRepository.ofIdentity(new FlightPlanId(PLAN_ID))).thenReturn(Optional.of(plan));
        when(weatherRepository.findWeatherDataById(1L)).thenReturn(Optional.of(weatherData));
        when(plan.weatherData()).thenAnswer(invocation -> List.copyOf(associated));
        when(plan.associateWeatherData(weatherData)).thenAnswer(invocation -> {
            associated.add(weatherData);
            return false;
        });
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final WeatherAssociationResult result =
                service.associateByIds(PLAN_ID, List.of(1L, 1L), "pilot", false);

        assertEquals(1, result.associatedWeatherCount());
        assertFalse(result.previousTestInvalidated());
        verify(plan, times(1)).associateWeatherData(weatherData);
    }
}
