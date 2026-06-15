package pt.isep.sem4pi.application.service.flightplan;

import org.junit.Before;
import org.junit.Test;
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
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightPlanRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryPilotRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateFlightPlanFromFileServiceTest {

    private static final IataCodeCompany TP = new IataCodeCompany("TP");

    private CreateFlightPlanFromFileService service;
    private InMemoryFlightPlanRepository flightPlanRepository;

    @Before
    public void setUp() {
        flightPlanRepository = new InMemoryFlightPlanRepository();
        final InMemoryFlightRouteRepository flightRouteRepository = new InMemoryFlightRouteRepository();
        final InMemoryAirTransportCompanyRepository companyRepository = new InMemoryAirTransportCompanyRepository();
        final InMemoryPilotRepository pilotRepository = new InMemoryPilotRepository();
        final InMemoryFlightRepository flightRepository = new InMemoryFlightRepository();

        final CreateFlightPlanService createFlightPlanService = new CreateFlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                companyRepository,
                pilotRepository,
                flightRepository
        );
        service = new CreateFlightPlanFromFileService(new ImportFlightPlanService(createFlightPlanService));

        final AirTransportCompany company = new AirTransportCompany(
                new CompanyName("TAP"),
                TP,
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
        pilotRepository.save(new Pilot(new PilotId("PILOT01"), TP));
        flightRouteRepository.save(new FlightRoute(
                new FlightRouteID("TP100"),
                TP,
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
    }

    @Test
    public void createFromFileImportsValidDsldig5() throws Exception {
        final Path file = writeTempFlightPlan(validFlightPlanContent());

        final FlightPlanImportResultDTO result = service.createFromFile(file.toString());

        assertTrue(result.isSuccess());
        assertNotNull(result.getFlightPlanId());
        assertTrue(flightPlanRepository.ofIdentity(new FlightPlanId(result.getFlightPlanId())).isPresent());
    }

    @Test
    public void validateFileAcceptsValidDsldig5() throws Exception {
        final int before = countFlightPlans();
        final Path file = writeTempFlightPlan(validFlightPlanContent());

        final FlightPlanImportResultDTO result = service.validateFile(file.toString());

        assertTrue(result.isSuccess());
        assertEquals(before, countFlightPlans());
    }

    @Test
    public void createFromFileRejectsUnsupportedFormat() throws Exception {
        final int before = countFlightPlans();
        final Path file = Files.createTempFile("flight-plan", ".json");
        Files.writeString(file, validFlightPlanContent());

        final FlightPlanImportResultDTO result = service.createFromFile(file.toString());

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().get(0).contains("Unsupported file format"));
        assertEquals(before, countFlightPlans());
    }

    @Test
    public void createFromFileRejectsUs120SemanticErrors() throws Exception {
        final int before = countFlightPlans();
        final Path file = writeTempFlightPlan(validFlightPlanContent().replace("15/6/", "32/13/"));

        final FlightPlanImportResultDTO result = service.createFromFile(file.toString());

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Invalid or non-existent date")));
        assertEquals(before, countFlightPlans());
    }

    @Test
    public void createFromFileRejectsSyntacticErrors() throws Exception {
        final int before = countFlightPlans();
        final Path file = writeTempFlightPlan(validFlightPlanContent().replace("};", ""));

        final FlightPlanImportResultDTO result = service.createFromFile(file.toString());

        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertEquals(before, countFlightPlans());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromFileRejectsMissingFile() {
        service.createFromFile("does-not-exist.dsldig5");
    }

    private String validFlightPlanContent() {
        final int year = LocalDate.now().getYear();
        return """
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
                """.formatted(year);
    }

    private Path writeTempFlightPlan(final String content) throws Exception {
        final Path file = Files.createTempFile("flight-plan", ".dsldig5");
        Files.writeString(file, content);
        return file;
    }

    private int countFlightPlans() {
        int count = 0;
        for (final FlightPlan ignored : flightPlanRepository.findAll()) {
            count++;
        }
        return count;
    }
}
