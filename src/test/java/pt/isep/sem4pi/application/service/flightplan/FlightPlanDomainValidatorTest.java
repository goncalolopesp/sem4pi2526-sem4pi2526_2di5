package pt.isep.sem4pi.application.service.flightplan;

import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class FlightPlanDomainValidatorTest {

    @Test
    public void ensureValidRouteAndLegPassValidation() {
        final FlightRoute route = new FlightRoute(
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

        final FlightLeg leg = new FlightLeg(
                new Departure(
                        new IataCodeAirport("OPO"),
                        new Date(20, 6, 2026),
                        new Time(14, 30, 0)
                ),
                new Arrival(new IataCodeAirport("MAD"), new Time(15, 20, 0)),
                List.of(),
                new Fuel(new FuelCapacity(4200.0), FuelUnit.KG)
        );

        final FlightPlan plan = Mockito.mock(FlightPlan.class);
        Mockito.when(plan.legs()).thenReturn(List.of(leg));

        assertTrue(FlightPlanDomainValidator.validate(plan, route).isEmpty());
    }
}
