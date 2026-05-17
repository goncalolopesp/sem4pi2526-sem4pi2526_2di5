package pt.isep.sem4pi.application.service.aircraft;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRepository;

import static org.junit.Assert.assertTrue;

public class AircraftRetirementServiceTest {

    @Test
    public void retiresAircraftWithoutPendingFlights() {
        final InMemoryAircraftRepository aircraftRepository = new InMemoryAircraftRepository();
        final InMemoryFlightRepository flightRepository = new InMemoryFlightRepository();
        final AircraftRetirementService service = new AircraftRetirementService(aircraftRepository, flightRepository);

        final AircraftRegistration registration = new AircraftRegistration("CS-OK");
        final Aircraft aircraft = new Aircraft(registration, new AircraftModelId("A320"));
        aircraftRepository.save(aircraft);

        final Aircraft retired = service.retireAircraft(registration);

        assertTrue(retired.isRetired());
    }

    @Test(expected = IllegalStateException.class)
    public void blocksRetirementWhenPendingFlightsExist() {
        final InMemoryAircraftRepository aircraftRepository = new InMemoryAircraftRepository();
        final InMemoryFlightRepository flightRepository = new InMemoryFlightRepository();
        final AircraftRetirementService service = new AircraftRetirementService(aircraftRepository, flightRepository);

        final AircraftRegistration registration = new AircraftRegistration("CS-BLOCK");
        final Aircraft aircraft = new Aircraft(registration, new AircraftModelId("A320"));
        aircraftRepository.save(aircraft);

        final Flight flight = new Flight(new FlightId("TP200"), registration);
        flightRepository.save(flight);

        service.retireAircraft(registration);
    }
}

