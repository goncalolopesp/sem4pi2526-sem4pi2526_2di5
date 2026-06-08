package pt.isep.sem4pi.application.service.route;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;

import java.time.LocalDate;

/**
 * Application service for deactivating flight routes.
 * 
 * This service orchestrates the deactivation of flight routes by checking
 * business rules (e.g., no planned flights after deactivation date) and
 * updating the route status through the repository.
 * 
 * Implements US074 rule: Routes cannot be deactivated if there are planned
 * flights scheduled after the deactivation date.
 * 
 * @author SEM4PI Team
 */
@ApplicationService
public class DeactivateFlightRouteService {

    /** Repository for flight route persistence */
    private final FlightRouteRepository routeRepository;
    
    /** Repository for accessing flight information */
    private final FlightRepository flightRepository;

    /**
     * Constructs a DeactivateFlightRouteService with the specified repositories.
     * 
     * @param routeRepository the repository used to persist flight routes
     * @param flightRepository the repository used to query flights
     */
    public DeactivateFlightRouteService(FlightRouteRepository routeRepository, FlightRepository flightRepository) {
        this.routeRepository = routeRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Deactivates a flight route on the specified date.
     * 
     * Performs the following steps:
     * 1. Looks up the route by ID
     * 2. Checks if there are planned flights after the deactivation date
     * 3. If flights exist, throws an IllegalStateException
     * 4. Otherwise, marks the route as deactivated and persists it
     * 
     * @param rawRouteId the route identifier as a string (e.g., "TP12")
     * @param deactivationDate the date when the route should be deactivated
     * @return the deactivated FlightRoute
     * 
     * @throws IllegalArgumentException if the route ID is invalid or the route is not found
     * @throws IllegalStateException if there are planned flights after the deactivation date
     */
    public FlightRoute deactivateRoute(String rawRouteId, LocalDate deactivationDate) {
        FlightRouteID routeId = new FlightRouteID(rawRouteId);

        FlightRoute route = routeRepository.ofIdentity(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found."));

        boolean hasFlights = flightRepository.hasFlightsForRouteAfterDate(routeId, deactivationDate);

        if (hasFlights) {
            throw new IllegalStateException("Cannot deactivate: there are planned flights for this route after the given date.");
        }

        route.deactivate(deactivationDate);

        return routeRepository.save(route);
    }

    /**
     * Validates whether a route can be deactivated on the specified date.
     * 
     * This method checks if there are planned flights for the route after the
     * deactivation date without actually performing the deactivation.
     * 
     * @param rawRouteId the route identifier as a string (e.g., "TP12")
     * @param deactivationDate the date to validate for deactivation
     * 
     * @throws IllegalArgumentException if the route ID is invalid
     * @throws IllegalStateException if there are planned flights after the deactivation date
     */
    public void validateDeactivation(String rawRouteId, LocalDate deactivationDate) {
        FlightRouteID routeId = new FlightRouteID(rawRouteId);
        boolean hasFlights = flightRepository.hasFlightsForRouteAfterDate(routeId, deactivationDate);

        if (hasFlights) {
            throw new IllegalStateException("Cannot deactivate: there are planned flights for this route after the given date.");
        }
    }

}