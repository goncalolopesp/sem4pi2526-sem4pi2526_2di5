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

@UseCaseController
public class CreateFlightRouteController {

    private final AuthorizationService authz;
    private final CreateFlightRouteService service;
    private final AirTransportCompanyRepository companyRepo;
    private final AirportRepository airportRepo;

    /* Construtor para produção (usado pela UI) */
    public CreateFlightRouteController() {
        this.authz = AuthzRegistry.authorizationService();

        // Assume-se que os métodos no factory têm estes nomes, ajusta se necessário
        this.companyRepo = PersistenceContext.repositories().airTransportCompanies();
        this.airportRepo = PersistenceContext.repositories().airports();
        FlightRouteRepository routeRepo = PersistenceContext.repositories().flightRoutes();

        this.service = new CreateFlightRouteService(routeRepo);
    }

    /* Construtor para testes (permite injeção de repositórios Mock) */
    public CreateFlightRouteController(AuthorizationService authz, CreateFlightRouteService service,
                                       AirTransportCompanyRepository companyRepo, AirportRepository airportRepo) {
        this.authz = authz;
        this.service = service;
        this.companyRepo = companyRepo;
        this.airportRepo = airportRepo;
    }

    public FlightRoute createFlightRoute(FlightRouteDTO flightRouteDTO) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return service.createFlightRoute(flightRouteDTO);
    }

    public Iterable<AirTransportCompany> getCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return companyRepo.findAll();
    }

    public Iterable<Airport> getAirports() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return airportRepo.findAll();
    }
}