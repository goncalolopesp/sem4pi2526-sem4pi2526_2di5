package pt.isep.sem4pi.application.service.flight;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;

public class FlightSchedulingService {

    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;

    public FlightSchedulingService(final AircraftRepository aircraftRepository,
                                   final FlightRepository flightRepository) {
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
    }

    public Flight scheduleFlight(final FlightId flightId, final AircraftRegistration registration) {
        final Aircraft aircraft = aircraftRepository.ofIdentity(registration)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found."));

        if (aircraft.isRetired()) {
            throw new IllegalStateException("Cannot schedule flight for retired aircraft.");
        }

        if (flightRepository.containsOfIdentity(flightId)) {
            throw new IllegalArgumentException("Flight already exists.");
        }

        final Flight flight = new Flight(flightId, registration);
        return flightRepository.save(flight);
    }
}

