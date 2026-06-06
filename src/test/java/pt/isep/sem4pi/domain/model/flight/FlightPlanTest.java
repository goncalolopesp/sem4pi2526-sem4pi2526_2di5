package pt.isep.sem4pi.domain.model.flight;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.flight.route.Route;
import pt.isep.sem4pi.domain.model.flight.route.RouteID;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FlightPlanTest {

    @Test
    public void ensureValidateChangesStatusFromDraftToValidated() {
        final FlightPlan plan = buildFlightPlan();

        plan.validate();

        assertEquals(FlightPlanStatus.VALIDATED, plan.status());
    }

    @Test
    public void ensureRejectChangesStatusFromDraftToRejected() {
        final FlightPlan plan = buildFlightPlan();

        plan.reject();

        assertEquals(FlightPlanStatus.REJECTED, plan.status());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureValidateFailsWhenPlanIsNotDraft() {
        final FlightPlan plan = buildFlightPlan();
        plan.validate();
        plan.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureRejectFailsWhenPlanIsNotDraft() {
        final FlightPlan plan = buildFlightPlan();
        plan.reject();
        plan.reject();
    }

    private FlightPlan buildFlightPlan() {
        final IataCodeCompany tp = new IataCodeCompany("TP");
        final RouteSegment segment = new RouteSegment(
                new Coordinates(41.0, -8.0),
                new Coordinates(40.0, -3.0),
                List.of(new AltitudeRange(8000, 10000, 300.0)),
                450,
                new Wind(90, 5.0)
        );
        final FlightRoute route = new FlightRoute(
                new FlightRouteID("TP100"),
                tp,
                new IataCodeAirport("OPO"),
                new IataCodeAirport("MAD"),
                List.of(segment)
        );
        final Aircraft aircraft = new Aircraft(
                new AircraftRegistration("CS-TST"),
                new AircraftModelId(new ManufacturerId("AIR"), "A320"),
                new CabinConfiguration(150, 20, 8),
                "PT",
                LocalDate.of(2015, 1, 1)
        );
        final Pilot pilot = new Pilot(new PilotId("PILOT01"), tp);
        final int year = LocalDate.now().getYear();
        final FlightLeg leg = new FlightLeg(
                new Departure(new IataCodeAirport("OPO"), new Date(15, 6, year), new Time(9, 0, 0)),
                new Arrival(new IataCodeAirport("MAD"), new Time(11, 0, 0)),
                List.of(),
                new Fuel(new FuelCapacity(4200.0), FuelUnit.KG)
        );

        return new FlightPlan(
                new FlightPlanId("FP-TEST"),
                new FlightId("TP100"),
                FlightType.REGULAR,
                route,
                List.of(leg),
                aircraft,
                pilot
        );
    }
}
