package pt.isep.sem4pi.application.controller.route;

import eapli.framework.application.UseCaseController;
import pt.isep.sem4pi.application.service.route.DeactivateFlightRouteService;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;

/**
 * Controller for flight route deactivation use case.
 *
 * This controller handles the UI layer for deactivating flight routes. It provides
 * methods to deactivate routes, retrieve existing routes, and validate whether
 * a route can be deactivated before actually performing the deactivation.
 *
 * @author SEM4PI Team
 */
@UseCaseController
public class DeactivateFlightRouteController {

    /** Service for deactivating flight routes */
    private final DeactivateFlightRouteService service;

    /** Repository for accessing flight routes */
    private final FlightRouteRepository routeRepository;

    /**
     * Default constructor that initializes the controller with default repositories
     * from the persistence layer.
     */
    public DeactivateFlightRouteController() {
        this.routeRepository = PersistenceContext.repositories().flightRoutes();
        this.service = new DeactivateFlightRouteService(
                routeRepository,
                PersistenceContext.repositories().flights()
        );
    }

    /**
     * Constructs a DeactivateFlightRouteController with the specified dependencies.
     *
     * @param service the flight route deactivation service
     * @param routeRepository the flight route repository
     */
    public DeactivateFlightRouteController(DeactivateFlightRouteService service,
                                            FlightRouteRepository routeRepository) {
        this.service = service;
        this.routeRepository = routeRepository;
    }

    /**
     * Deactivates a flight route on the specified date.
     *
     * @param routeId the identifier of the route to deactivate (e.g., "TP12")
     * @param deactivationDate the date when the route should be deactivated
     * @return the deactivated FlightRoute
     *
     * @throws IllegalArgumentException if the route ID is invalid or the route is not found
     * @throws IllegalStateException if there are planned flights after the deactivation date
     */
    public FlightRoute deactivateRoute(String routeId, LocalDate deactivationDate) {
        return service.deactivateRoute(routeId, deactivationDate);
    }

    /**
     * Retrieves all flight routes in the system.
     *
     * @return an iterable of all FlightRoute objects
     */
    public Iterable<FlightRoute> getRoutes() {
        return routeRepository.findAll();
    }

    /**
     * Validates whether a route can be deactivated on the specified date.
     *
     * This method performs the necessary validations without actually performing
     * the deactivation, allowing the UI to show error messages before attempting
     * the actual operation.
     *
     * @param routeId the identifier of the route to validate (e.g., "TP12")
     * @param deactivationDate the date to validate for deactivation
     *
     * @throws IllegalArgumentException if the route ID is invalid
     * @throws IllegalStateException if there are planned flights after the deactivation date
     */
    public void validateDeactivation(String routeId, LocalDate deactivationDate) {
        service.validateDeactivation(routeId, deactivationDate);
    }
}