package pt.isep.sem4pi.application.controller.route;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.route.CreateFlightRouteService;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AirportRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

/**
 * Controller for flight route creation use case.
 * 
 * This controller handles the UI layer for creating new flight routes. It performs
 * authorization checks and delegates the actual business logic to the service.
 * It also provides methods to retrieve lists of active companies and airports
 * for use in the UI.
 * 
 * Only users with BACKOFFICE_OPERATOR or ADMIN roles can perform operations
 * through this controller.
 * 
 * @author SEM4PI Team
 */
@UseCaseController
public class CreateFlightRouteController {

    /** Authorization service for checking user permissions */
    private final AuthorizationService authz;
    
    /** Service for creating flight routes */
    private final CreateFlightRouteService service;
    
    /** Repository for accessing airline companies */
    private final AirTransportCompanyRepository companyRepo;
    
    /** Repository for accessing airports */
    private final AirportRepository airportRepo;

    /**
     * Default constructor that initializes the controller with default repositories
     * from the persistence layer.
     */
    public CreateFlightRouteController() {
        this.authz = AuthzRegistry.authorizationService();

        // Assume-se que os métodos no factory têm estes nomes, ajusta se necessário
        this.companyRepo = PersistenceContext.repositories().airTransportCompanies();
        this.airportRepo = PersistenceContext.repositories().airports();
        FlightRouteRepository routeRepo = PersistenceContext.repositories().flightRoutes();

        this.service = new CreateFlightRouteService(routeRepo);
    }

    /**
     * Constructs a CreateFlightRouteController with the specified dependencies.
     * 
     * @param authz the authorization service
     * @param service the flight route creation service
     * @param companyRepo the company repository
     * @param airportRepo the airport repository
     */
    public CreateFlightRouteController(AuthorizationService authz, CreateFlightRouteService service,
                                       AirTransportCompanyRepository companyRepo, AirportRepository airportRepo) {
        this.authz = authz;
        this.service = service;
        this.companyRepo = companyRepo;
        this.airportRepo = airportRepo;
    }

    /**
     * Creates a new flight route using the provided DTO.
     * 
     * Requires the user to have BACKOFFICE_OPERATOR or ADMIN role.
     * 
     * @param flightRouteDTO the data transfer object containing route information
     * @return the created FlightRoute
     * 
     * @throws AuthorizationException if the user doesn't have the required roles
     * @throws IllegalArgumentException if the DTO data is invalid
     */
    public FlightRoute createFlightRoute(FlightRouteDTO flightRouteDTO) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, SEM4PIRoles.ADMIN);
        return service.createFlightRoute(flightRouteDTO);
    }

    /**
     * Retrieves all air transport companies.
     * 
     * Requires the user to have BACKOFFICE_OPERATOR or ADMIN role.
     * 
     * @return an iterable of all AirTransportCompany objects
     * 
     * @throws AuthorizationException if the user doesn't have the required roles
     */
    public Iterable<AirTransportCompany> getCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, SEM4PIRoles.ADMIN);
        return companyRepo.findAll();
    }

    /**
     * Retrieves all airports.
     * 
     * Requires the user to have BACKOFFICE_OPERATOR or ADMIN role.
     * 
     * @return an iterable of all Airport objects
     * 
     * @throws AuthorizationException if the user doesn't have the required roles
     */
    public Iterable<Airport> getAirports() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, SEM4PIRoles.ADMIN);
        return airportRepo.findAll();
    }
}