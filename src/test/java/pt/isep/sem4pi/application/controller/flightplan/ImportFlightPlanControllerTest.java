package pt.isep.sem4pi.application.controller.flightplan;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.application.service.flightplan.CreateFlightPlanService;
import pt.isep.sem4pi.application.service.flightplan.ImportFlightPlanService;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightPlanRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryPilotRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ImportFlightPlanControllerTest {

    @Test
    public void ensureImportFlightPlanDelegatesAndAuthorizes() throws Exception {
        final InMemoryFlightPlanRepository flightPlanRepository = new InMemoryFlightPlanRepository();
        final InMemoryFlightRouteRepository flightRouteRepository = new InMemoryFlightRouteRepository();
        final InMemoryAirTransportCompanyRepository companyRepository = new InMemoryAirTransportCompanyRepository();
        final InMemoryPilotRepository pilotRepository = new InMemoryPilotRepository();
        final InMemoryFlightRepository flightRepository = new InMemoryFlightRepository();

        final IataCodeCompany tp = new IataCodeCompany("TP");
        final AirTransportCompany company = new AirTransportCompany(
                new CompanyName("TAP"),
                tp,
                new IcaoCodeCompany("TAP")
        );
        company.addAircraftToFleet(new Aircraft(
                new AircraftRegistration("CS-TST"),
                new AircraftModelId(new ManufacturerId("AIR"), "A320"),
                new CabinConfiguration(150, 20, 8),
                "PT",
                LocalDate.of(2015, 1, 1)
        ));
        companyRepository.save(company);
        pilotRepository.save(new Pilot(new PilotId("PILOT01"), tp));
        flightRouteRepository.save(new FlightRoute(
                new FlightRouteID("TP100"),
                tp,
                new IataCodeAirport("OPO"),
                new IataCodeAirport("MAD"),
                List.of(new RouteSegment(
                        new Coordinates(41.0, -8.0),
                        new Coordinates(40.0, -3.0),
                        List.of(new AltitudeRange(8000, 10000, 300.0)),
                        450,
                        new Wind(90, 5.0)
                ))
        ));
        flightRepository.save(new Flight(
                new FlightId("TP100"),
                new AircraftRegistration("CS-TST"),
                new FlightRouteID("TP100"),
                LocalDate.of(LocalDate.now().getYear(), 6, 15)
        ));

        final CreateFlightPlanService createFlightPlanService = new CreateFlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                companyRepository,
                pilotRepository,
                flightRepository
        );
        final ImportFlightPlanService service = new ImportFlightPlanService(createFlightPlanService);
        final AuthorizationService authz = mock(AuthorizationService.class);
        final ImportFlightPlanController controller = new ImportFlightPlanController(authz, service);

        final int year = LocalDate.now().getYear();
        final Path file = Files.createTempFile("flight-plan", ".dsldig5");
        Files.writeString(file, """
                flight TP100 regular {
                aircraft CS-TST;
                pilot PILOT01;
                route TP100 { (41.0|-8.0) (40.0|-3.0) };
                leg {
                departure { OPO, 15/6/%d, 09:00 };
                arrival { MAD, 11:00 };
                segment { (41.0|-8.0), (40.0|-3.0), {(8000,10000)}, 450, 90, 5 };
                fuel { 4200, kg };
                };
                };
                """.formatted(year));

        final FlightPlanImportResultDTO result = controller.importFlightPlan(file.toString());

        assertTrue(result.isSuccess());
        verify(authz).ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.PILOT, SEM4PIRoles.ADMIN);
    }
}
