package pt.isep.sem4pi.application.service.flight;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;

import java.time.LocalDate;

public class FlightSchedulingService {

    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final FlightRouteRepository routeRepository;

    public FlightSchedulingService(final AircraftRepository aircraftRepository,
                                   final FlightRepository flightRepository,
                                   final FlightRouteRepository routeRepository) {
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Schedules a new flight with aircraft, route, and departure date validation.
     */
    public Flight scheduleFlight(final FlightId flightId, final AircraftRegistration registration,
                                 final FlightRouteID routeId, final LocalDate departureDate) {

        final Aircraft aircraft = aircraftRepository.ofIdentity(registration)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found."));

        if (aircraft.isRetired()) {
            throw new IllegalStateException("Cannot schedule flight for retired aircraft.");
        }

        if (flightRepository.containsOfIdentity(flightId)) {
            throw new IllegalArgumentException("Flight already exists.");
        }

        /* 1. Fetch the route and validate its activation status for the departure date */
        final FlightRoute route = routeRepository.ofIdentity(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found."));

        if (!route.isActiveOn(departureDate)) {
            throw new IllegalStateException("Cannot schedule flight: Route is deactivated for this date.");
        }

        // Instantiates the flight with the mandatory route and date parameters
        final Flight flight = new Flight(flightId, registration, routeId, departureDate);
        return flightRepository.save(flight);
    }
}