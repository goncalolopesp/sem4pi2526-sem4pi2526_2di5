package pt.isep.sem4pi.application.service.aircraft;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraftModel.*;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftModelRepository;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.Assert.*;

public class AddAircraftServiceTest {

    private InMemoryAirTransportCompanyRepository companyRepo;
    private InMemoryAircraftModelRepository modelRepo;
    private AddAircraftService service;

    @Before
    public void setUp() {
        companyRepo = new InMemoryAirTransportCompanyRepository();
        modelRepo = new InMemoryAircraftModelRepository();
        service = new AddAircraftService(companyRepo, modelRepo);

        companyRepo.save(new AirTransportCompany(new CompanyName("TAP"), new IataCode("TP"), new IcaoCode("TAP")));

        Manufacturer manufacturer = new Manufacturer(new ManufacturerId("AIRBUS"), "Airbus", "France");
        EngineModel engine = new EngineModel(new EngineModelId("CFM56"), manufacturer, "CFM56",
                EngineType.TURBOFAN, new BigDecimal("100"), FuelType.JET_A1, new BigDecimal("100"));

        AircraftModel dummyModel = new AircraftModel(
                new AircraftModelId(new ManufacturerId("AIRBUS"), "A320"),
                AircraftType.PASSENGER, EngineType.TURBOFAN,
                new Weight(new BigDecimal("10000"), new BigDecimal("20000"), new BigDecimal("30000")),
                new Coefficient(new BigDecimal("0.5"), new BigDecimal("1.5")), // <-- Valores corrigidos e seguros
                new AircraftPerformance(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE),
                new Fuel(BigDecimal.ONE),
                new WingGeometry(BigDecimal.ONE, BigDecimal.ONE),
                Collections.singletonList(engine),
                180
        );

        modelRepo.save(dummyModel);
    }

    @Test
    public void ensureSuccessfulAircraftAddition() {
        service.addAircraftToFleet("TP", "CS-TPA", "AIRBUS", "A320", "Portugal", 150, 0, 0);

        AirTransportCompany tap = companyRepo.findByIataCode(new IataCode("TP")).get();
        assertEquals(1, tap.getFleet().size());
    }

    @Test
    public void ensureFailsIfSeatsExceedModelCapacity() {
        try {
            service.addAircraftToFleet("TP", "CS-TPB", "AIRBUS", "A320", "Portugal", 200, 0, 0);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exceed the maximum capacity"));
        }
    }

    @Test
    public void ensureFailsIfRegistrationAlreadyExists() {
        service.addAircraftToFleet("TP", "CS-TPA", "AIRBUS", "A320", "Portugal", 150, 0, 0);

        try {
            service.addAircraftToFleet("TP", "CS-TPA", "AIRBUS", "A320", "Portugal", 100, 0, 0);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("already exists"));
        }
    }
}