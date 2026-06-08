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
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanStatus;
import pt.isep.sem4pi.domain.model.flight.FlightType;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.*;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightPlanRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryFlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryPilotRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CreateFlightPlanServiceTest {

    private static final IataCodeCompany TP = new IataCodeCompany("TP");

    private CreateFlightPlanService service;
    private InMemoryFlightPlanRepository flightPlanRepository;
    private InMemoryPilotRepository pilotRepository;
    private InMemoryAirTransportCompanyRepository companyRepository;
    private InMemoryFlightRepository flightRepository;
    private Aircraft fleetAircraft;

    @Before
    public void setUp() {
        flightPlanRepository = new InMemoryFlightPlanRepository();
        final InMemoryFlightRouteRepository flightRouteRepository = new InMemoryFlightRouteRepository();
        companyRepository = new InMemoryAirTransportCompanyRepository();
        pilotRepository = new InMemoryPilotRepository();
        flightRepository = new InMemoryFlightRepository();

        service = new CreateFlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                companyRepository,
                pilotRepository,
                flightRepository
        );

        final AirTransportCompany company = new AirTransportCompany(
                new CompanyName("TAP"),
                TP,
                new IcaoCodeCompany("TAP")
        );
        fleetAircraft = new Aircraft(
                new AircraftRegistration("CS-TST"),
                new AircraftModelId(new ManufacturerId("AIR"), "A320"),
                new CabinConfiguration(150, 20, 8),
                "PT",
                LocalDate.of(2015, 1, 1)
        );
        company.addAircraftToFleet(fleetAircraft);
        companyRepository.save(company);

        pilotRepository.save(new Pilot(new PilotId("PILOT01"), TP));

        final RouteSegment segment = new RouteSegment(
                new Coordinates(41.0, -8.0),
                new Coordinates(40.0, -3.0),
                List.of(new AltitudeRange(8000, 10000, 300.0)),
                450,
                new Wind(90, 5.0)
        );
        flightRouteRepository.save(new FlightRoute(
                new FlightRouteID("TP100"),
                TP,
                new IataCodeAirport("OPO"),
                new IataCodeAirport("MAD"),
                List.of(segment)
        ));

        flightRepository.save(new Flight(
                new FlightId("TP100"),
                new AircraftRegistration("CS-TST"),
                new FlightRouteID("TP100"),
                LocalDate.of(LocalDate.now().getYear(), 6, 15)
        ));
    }

    @Test
    public void ensureCreateFlightPlanSetsDraftStatusAndAutoId() {
        final FlightPlan plan = service.createFlightPlan(validDto());

        assertEquals(FlightPlanStatus.DRAFT, plan.status());
        assertTrue(plan.identity().value().matches("FP-\\d{6}"));
        assertEquals(FlightType.REGULAR, plan.flightType());
        assertEquals("TP100", plan.flightId().value());
        assertEquals("CS-TST", plan.aircraft().identity().value());
        assertEquals("PILOT01", plan.pilot().identity().code());
        assertEquals("TP100", plan.route().identity().code());
        assertEquals(1, plan.legs().size());
    }

    @Test
    public void ensureCreateFlightPlanUsesAircraftFromSelectedFlight() {
        final FlightPlan plan = service.createFlightPlan(validDto());

        assertEquals("CS-TST", plan.aircraft().identity().value());
    }

    @Test
    public void ensureCreateFlightPlanGeneratesSequentialIds() {
        final int before = countFlightPlans();
        final FlightPlan first = service.createFlightPlan(validDto());
        final FlightPlan second = service.createFlightPlan(validDto());

        assertNotEquals(first.identity().value(), second.identity().value());
        assertEquals(before + 2, countFlightPlans());
        assertTrue(extractIdNumber(second.identity().value()) > extractIdNumber(first.identity().value()));
    }

    @Test
    public void ensureListFlightPlansReturnsSavedPlans() {
        final int before = countFlightPlans();
        service.createFlightPlan(validDto());
        service.createFlightPlan(validDto());

        assertEquals(before + 2, service.listFlightPlans().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsNullDto() {
        service.createFlightPlan(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsUnknownRoute() {
        final CreateFlightPlanDTO dto = validDto();
        dto.setRouteId("XX999");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsUnknownFlight() {
        final CreateFlightPlanDTO dto = validDto();
        dto.setFlightId("UNKNOWN");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsUnknownPilot() {
        final CreateFlightPlanDTO dto = validDto();
        dto.setPilotId("UNKNOWN");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsPilotFromOtherCompany() {
        pilotRepository.save(new Pilot(new PilotId("OTHER"), new IataCodeCompany("FR")));

        final CreateFlightPlanDTO dto = validDto();
        dto.setPilotId("OTHER");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsDuplicateManualId() {
        final CreateFlightPlanDTO dto = validDto();
        dto.setFlightPlanId("FP-MANUAL");
        service.createFlightPlan(dto);
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsMismatchedDepartureAirport() {
        final CreateFlightPlanDTO dto = validDto();
        dto.getLegs().get(0).setDepartureAirportIata("LIS");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsMismatchedArrivalAirport() {
        final CreateFlightPlanDTO dto = validDto();
        dto.getLegs().get(0).setArrivalAirportIata("LIS");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsEmptyLegs() {
        final CreateFlightPlanDTO dto = validDto();
        dto.setLegs(Collections.emptyList());
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsInvalidFlightType() {
        final CreateFlightPlanDTO dto = validDto();
        dto.setFlightType("INVALID");
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsNonPositiveFuel() {
        final CreateFlightPlanDTO dto = validDto();
        dto.getLegs().get(0).setFuelQuantity(0.0);
        service.createFlightPlan(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightPlanRejectsRetiredAircraft() {
        fleetAircraft.retire();
        companyRepository.save(companyRepository.findByIataCode(TP).orElseThrow());

        service.createFlightPlan(validDto());
    }

    private int countFlightPlans() {
        int count = 0;
        for (final FlightPlan ignored : flightPlanRepository.findAll()) {
            count++;
        }
        return count;
    }

    private long extractIdNumber(final String planId) {
        return Long.parseLong(planId.substring(3));
    }

    private CreateFlightPlanDTO validDto() {
        final int year = LocalDate.now().getYear();
        final CreateFlightPlanLegDTO leg = new CreateFlightPlanLegDTO(
                "OPO",
                15, 6, year,
                9, 0, 0,
                "MAD",
                11, 0, 0,
                4200.0,
                "KG"
        );
        return new CreateFlightPlanDTO(
                null,
                "TP100",
                "TP100",
                "REGULAR",
                null,
                "PILOT01",
                List.of(leg)
        );
    }
}
