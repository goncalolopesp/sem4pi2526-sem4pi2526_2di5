package pt.isep.sem4pi.application.service.aircraft;

import org.junit.Test;
import pt.isep.sem4pi.domain.aircraft.Aircraft;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.aircraft.CabinConfiguration; // <-- Adicionado o Import
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.flight.Flight;
import pt.isep.sem4pi.domain.flight.FlightId;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRepository;

import java.time.LocalDate;

import static org.junit.Assert.assertTrue;

public class AircraftRetirementServiceTest {

    @Test
    public void retiresAircraftWithoutPendingFlights() {
        final InMemoryAircraftRepository aircraftRepository = new InMemoryAircraftRepository();
        final InMemoryFlightRepository flightRepository = new InMemoryFlightRepository();
        final AircraftRetirementService service = new AircraftRetirementService(aircraftRepository, flightRepository);

        final AircraftRegistration registration = new AircraftRegistration("CS-OK");

        final CabinConfiguration dummySeats = new CabinConfiguration(150, 20, 0);
        final Aircraft aircraft = new Aircraft(registration, new AircraftModelId(new ManufacturerId("Airbus"),"A320"), dummySeats, "Portugal", LocalDate.of(2020, 1, 1));
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

        final CabinConfiguration dummySeats = new CabinConfiguration(150, 20, 0);
        final Aircraft aircraft = new Aircraft(registration, new AircraftModelId(new ManufacturerId("Airbus"),"A320"), dummySeats, "Portugal", LocalDate.of(2020, 1, 1));
        aircraftRepository.save(aircraft);

        final Flight flight = new Flight(new FlightId("TP200"), registration);
        flightRepository.save(flight);

        service.retireAircraft(registration);
    }
}