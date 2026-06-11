package pt.isep.sem4pi.application.service.flightplan;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.Elevation;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airport.IcaoCodeAirport;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.flight.FlightPlanStatus;
import pt.isep.sem4pi.domain.model.flight.FlightType;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;
import pt.isep.sem4pi.domain.repositories.AirportRepository;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.infrastructure.simulation.FlightPlanJsonExporter;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestFlightPlanServiceTest {

    private FlightPlanRepository flightPlanRepository;
    private FlightRouteRepository flightRouteRepository;
    private AirportRepository airportRepository;
    private AirControlAreaRepository airControlAreaRepository;
    private FlightTestRunner flightTestRunner;
    private FlightPlanJsonExporter jsonExporter;
    private TestFlightPlanService service;

    @Before
    public void setUp() throws Exception {
        flightPlanRepository = Mockito.mock(FlightPlanRepository.class);
        flightRouteRepository = Mockito.mock(FlightRouteRepository.class);
        airportRepository = Mockito.mock(AirportRepository.class);
        airControlAreaRepository = Mockito.mock(AirControlAreaRepository.class);
        flightTestRunner = Mockito.mock(FlightTestRunner.class);
        jsonExporter = Mockito.mock(FlightPlanJsonExporter.class);
        when(jsonExporter.export(any(FlightPlan.class), any(FlightRoute.class)))
                .thenReturn(Path.of("scomp/data/generated/FP-001.json"));
        service = new TestFlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                airportRepository,
                airControlAreaRepository,
                flightTestRunner,
                jsonExporter
        );
    }

    @Test
    public void ensureTestFlightPlanValidatesAndUpdatesStatusWhenChecksPass() throws Exception {
        final FlightPlan plan = validDraftPlan();
        final FlightRoute route = validRoute();
        when(flightPlanRepository.ofIdentity(new FlightPlanId("FP-001"))).thenReturn(Optional.of(plan));
        when(flightRouteRepository.ofIdentity(any(FlightRouteID.class))).thenReturn(Optional.of(route));
        stubDestinationLookup("MAD", "MAD_FIR", 1200.0);

        when(flightTestRunner.run(anyString(), anyDouble(), anyDouble(), anyInt(), anyString()))
                .thenReturn(new FlightTestExecutionResult(true, "scomp/reports/FP-001-test.txt", "PASSED"));

        final TestFlightPlanResultDTO result = service.testFlightPlan("FP-001");

        assertTrue(result.accepted());
        assertTrue(result.dslValidation().isValid());
        assertTrue(result.flightTest().passed());
        verify(plan, times(1)).validate();
        verify(plan, never()).reject();
        verify(flightPlanRepository, times(1)).save(plan);
        verify(jsonExporter, times(1)).export(plan, route);
    }

    @Test
    public void ensureTestFlightPlanRejectsWhenCFlightTestFails() throws Exception {
        final FlightPlan plan = validDraftPlan();
        final FlightRoute route = validRoute();
        when(flightPlanRepository.ofIdentity(new FlightPlanId("FP-002"))).thenReturn(Optional.of(plan));
        when(flightRouteRepository.ofIdentity(any(FlightRouteID.class))).thenReturn(Optional.of(route));
        stubDestinationLookup("MAD", "MAD_FIR", 1200.0);

        when(flightTestRunner.run(anyString(), anyDouble(), anyDouble(), anyInt(), anyString()))
                .thenReturn(new FlightTestExecutionResult(false, "scomp/reports/FP-002-test.txt", "FAILED"));

        final TestFlightPlanResultDTO result = service.testFlightPlan("FP-002");

        assertFalse(result.accepted());
        assertTrue(result.dslValidation().isValid());
        assertFalse(result.flightTest().passed());
        verify(plan, times(1)).reject();
        verify(plan, never()).validate();
    }

    @Test
    public void ensureTestFlightPlanRejectsWhenPlanValidationFails() throws Exception {
        final FlightPlan plan = Mockito.mock(FlightPlan.class);
        final FlightRoute route = validRoute();
        when(plan.status()).thenReturn(FlightPlanStatus.DRAFT);
        when(plan.route()).thenReturn(route);
        when(plan.legs()).thenReturn(List.of());
        when(flightPlanRepository.ofIdentity(new FlightPlanId("FP-003"))).thenReturn(Optional.of(plan));
        when(flightRouteRepository.ofIdentity(any(FlightRouteID.class))).thenReturn(Optional.of(route));

        final TestFlightPlanResultDTO result = service.testFlightPlan("FP-003");

        assertFalse(result.accepted());
        assertFalse(result.dslValidation().isValid());
        assertFalse(result.flightTest().executed());
        verify(plan, times(1)).reject();
        verify(flightTestRunner, never()).run(anyString(), anyDouble(), anyDouble(), anyInt(), anyString());
        verify(jsonExporter, never()).export(any(), any());
    }

    private FlightPlan validDraftPlan() {
        final FlightRoute route = validRoute();
        final FlightPlan plan = Mockito.mock(FlightPlan.class);
        when(plan.status()).thenReturn(FlightPlanStatus.DRAFT);
        when(plan.flightType()).thenReturn(FlightType.REGULAR);
        when(plan.identity()).thenReturn(new FlightPlanId("FP-001"));
        when(plan.flightId()).thenReturn(new FlightId("TP100"));
        when(plan.route()).thenReturn(route);
        when(plan.legs()).thenReturn(List.of(validLeg()));
        return plan;
    }

    private FlightRoute validRoute() {
        return new FlightRoute(
                new FlightRouteID("TP1001"),
                new IataCodeCompany("TP"),
                new IataCodeAirport("OPO"),
                new IataCodeAirport("MAD"),
                List.of(
                        new RouteSegment(
                                new Coordinates(41.24, -8.67),
                                new Coordinates(40.47, -3.56),
                                List.of(new AltitudeRange(30000, 35000, 10.0)),
                                850,
                                new Wind(45, 10.0)
                        )
                )
        );
    }

    private FlightLeg validLeg() {
        return new FlightLeg(
                new Departure(
                        new IataCodeAirport("OPO"),
                        new Date(20, 6, 2026),
                        new Time(14, 30, 0)
                ),
                new Arrival(new IataCodeAirport("MAD"), new Time(15, 20, 0)),
                List.of(),
                new Fuel(new FuelCapacity(4200.0), FuelUnit.KG)
        );
    }

    private void stubDestinationLookup(final String iata, final String areaCode, final double minFuel) {
        final Airport airport = new Airport(
                "Madrid",
                "Madrid",
                "ES",
                new IataCodeAirport(iata),
                new IcaoCodeAirport("LEMD"),
                new Coordinates(40.0, -3.5),
                new Elevation(610),
                new AirControlAreaID(areaCode)
        );
        when(airportRepository.ofIdentity(new IataCodeAirport(iata))).thenReturn(Optional.of(airport));

        final AirControlArea area = new AirControlArea(
                areaCode,
                new AirControlAreaBoundary(36.0, 42.0, -9.5, -6.0),
                "Test Area",
                minFuel
        );
        when(airControlAreaRepository.ofIdentity(new AirControlAreaID(areaCode))).thenReturn(Optional.of(area));
    }
}
