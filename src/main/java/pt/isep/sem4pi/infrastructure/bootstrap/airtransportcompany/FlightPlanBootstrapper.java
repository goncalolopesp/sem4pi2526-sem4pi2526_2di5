package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.flight.FlightType;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;

public class FlightPlanBootstrapper {

    private static final FlightPlanId DEMO_PLAN_ID = new FlightPlanId("FP-DEMO-001");

    public void execute() {
        final var repositories = PersistenceContext.repositories();
        final var flightPlanRepository = repositories.flightPlans();
        if (flightPlanRepository.ofIdentity(DEMO_PLAN_ID).isPresent()) {
            return;
        }

        final var route = repositories.flightRoutes()
                .ofIdentity(new FlightRouteID("TP1001"))
                .orElseThrow(() -> new IllegalStateException("Bootstrap route TP1001 not found."));
        final var aircraft = repositories.aircrafts()
                .ofIdentity(new AircraftRegistration("CS-TST"))
                .orElseThrow(() -> new IllegalStateException("Bootstrap aircraft CS-TST not found."));
        final var pilot = repositories.pilots()
                .ofIdentity(new PilotId("PILOT01"))
                .orElseThrow(() -> new IllegalStateException("Bootstrap pilot PILOT01 not found."));
        final var flight = repositories.flights()
                .ofIdentity(new FlightId("TP100"))
                .orElseThrow(() -> new IllegalStateException("Bootstrap flight TP100 not found."));

        final LocalDate departureDate = flight.departureDate();
        final FlightLeg leg = new FlightLeg(
                new Departure(
                        new IataCodeAirport("OPO"),
                        new Date(departureDate.getDayOfMonth(), departureDate.getMonthValue(),
                                departureDate.getYear()),
                        new Time(9, 0, 0)),
                new Arrival(new IataCodeAirport("MAD"), new Time(11, 0, 0)),
                List.of(),
                new Fuel(new FuelCapacity(4200.0), FuelUnit.KG));

        flightPlanRepository.save(new FlightPlan(
                DEMO_PLAN_ID,
                flight.identity(),
                FlightType.REGULAR,
                route,
                List.of(leg),
                aircraft,
                pilot));

        System.out.println("   -> Bootstrapped Flight Plan: " + DEMO_PLAN_ID.value());
    }
}
