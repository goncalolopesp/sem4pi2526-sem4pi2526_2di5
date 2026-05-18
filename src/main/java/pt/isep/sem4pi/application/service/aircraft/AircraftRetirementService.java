package pt.isep.sem4pi.application.service.aircraft;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;

public class AircraftRetirementService {

    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;

    public AircraftRetirementService(final AircraftRepository aircraftRepository,
                                     final FlightRepository flightRepository) {
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Retira a aeronave se nao existirem voos pendentes.
     */
    public Aircraft retireAircraft(final AircraftRegistration registration) {
        final Aircraft aircraft = aircraftRepository.ofIdentity(registration)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found."));

        if (flightRepository.hasPendingFlights(registration)) {
            throw new IllegalStateException("Cannot retire aircraft with pending flights.");
        }

        aircraft.retire();
        return aircraftRepository.save(aircraft);
    }
}
