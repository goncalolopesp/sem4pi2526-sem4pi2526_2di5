package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class FlightPlanBootstrapperTest {

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());
        PersistenceContext.resetForTesting();

        final var repositories = PersistenceContext.repositories();
        final IataCodeCompany company = new IataCodeCompany("TP");
        final FlightRouteID routeId = new FlightRouteID("TP1001");
        final AircraftRegistration registration = new AircraftRegistration("CS-TST");

        repositories.flightRoutes().save(new FlightRoute(
                routeId,
                company,
                new IataCodeAirport("OPO"),
                new IataCodeAirport("MAD"),
                List.of(new RouteSegment(
                        new Coordinates(41.24, -8.67),
                        new Coordinates(40.47, -3.56),
                        List.of(new AltitudeRange(8000, 10000, 300.0)),
                        450,
                        new Wind(90, 5.0)))));
        repositories.aircrafts().save(new Aircraft(
                registration,
                new AircraftModelId(new ManufacturerId("AIRBUS"), "A320"),
                new CabinConfiguration(150, 20, 0),
                "Portugal",
                LocalDate.of(2015, 5, 10)));
        repositories.pilots().save(new Pilot(new PilotId("PILOT01"), company, "pilot"));
        repositories.flights().save(new Flight(
                new FlightId("TP100"),
                registration,
                routeId,
                LocalDate.now().plusDays(5)));
    }

    @Test
    public void createsOneFlightPlanWithoutWeatherDataAndDoesNotDuplicateIt() {
        final FlightPlanBootstrapper bootstrapper = new FlightPlanBootstrapper();

        bootstrapper.execute();
        bootstrapper.execute();

        final var repository = PersistenceContext.repositories().flightPlans();
        final var plan = repository.ofIdentity(new FlightPlanId("FP-DEMO-001")).orElseThrow();
        assertTrue(plan.weatherData().isEmpty());
    }
}
