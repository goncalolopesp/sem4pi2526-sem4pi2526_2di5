package pt.isep.sem4pi.application.controller.route;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.route.DeactivateFlightRouteService;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
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

    /** Authorization service for checking user permissions */
    private final AuthorizationService authz;

    /** Service for deactivating flight routes */
    private final DeactivateFlightRouteService service;

    /** Repository for accessing flight routes */
    private final FlightRouteRepository routeRepository;

    /**
     * Default constructor that initializes the controller with default repositories
     * from the persistence layer.
     */
    public DeactivateFlightRouteController() {
        this.authz = AuthzRegistry.authorizationService();
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
        this(AuthzRegistry.authorizationService(), service, routeRepository);
    }

    public DeactivateFlightRouteController(AuthorizationService authz,
                                           DeactivateFlightRouteService service,
                                           FlightRouteRepository routeRepository) {
        this.authz = authz;
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
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                SEM4PIRoles.ADMIN
        );
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