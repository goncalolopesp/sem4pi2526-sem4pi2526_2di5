package pt.isep.sem4pi.application.service.engineModel;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryEngineModelRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryManufacturerRepository;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class EngineModelRegistrationServiceTest {

    private EngineModelRegistrationService service;
    private InMemoryEngineModelRepository engineModelRepository;
    private InMemoryManufacturerRepository manufacturerRepository;

    @Before
    public void setUp() {
        engineModelRepository = new InMemoryEngineModelRepository();
        manufacturerRepository = new InMemoryManufacturerRepository();
        service = new EngineModelRegistrationService(engineModelRepository, manufacturerRepository);

        final Manufacturer manufacturer = new Manufacturer(new ManufacturerId("CFM"), "CFM International", "France");
        manufacturerRepository.save(manufacturer);
    }

    @Test
    public void registerCreatesEngineModel() {
        final String modelCode = "CFM56-5B-" + System.nanoTime();
        final String modelName = "CFM56-5B-" + System.nanoTime();

        final EngineModel engineModel = service.register(
                modelCode,
                "CFM",
                modelName,
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );

        assertNotNull(engineModel);
        assertTrue(engineModelRepository.containsOfIdentity(new EngineModelId(modelCode)));
        assertTrue(engineModelRepository.existsWithManufacturerAndName(new ManufacturerId("CFM"), modelName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerFailsWhenManufacturerIsMissing() {
        service.register(
                "TRENT1000",
                "RR",
                "Trent 1000",
                EngineType.TURBOFAN,
                new BigDecimal("330.00"),
                FuelType.JET_A1,
                new BigDecimal("41.20")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsDuplicateEngineModelCode() {
        final String modelCode = "CFM56-5B-" + System.nanoTime();

        service.register(
                modelCode,
                "CFM",
                "CFM56-5B",
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );

        service.register(
                modelCode,
                "CFM",
                "CFM56-5B-ALT",
                EngineType.TURBOFAN,
                new BigDecimal("121.00"),
                FuelType.JET_A1,
                new BigDecimal("37.60")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsDuplicateNameForSameManufacturer() {
        final String modelName = "CFM56-5B-" + System.nanoTime();

        service.register(
                "CFM56-5B",
                "CFM",
                modelName,
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );

        service.register(
                "CFM56-5B-2",
                "CFM",
                modelName,
                EngineType.TURBOFAN,
                new BigDecimal("122.00"),
                FuelType.JET_A1,
                new BigDecimal("37.70")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsDuplicateNameIgnoringCase() {
        final String modelName = "CFM56-5B-" + System.nanoTime();

        service.register(
                "CFM56-5B",
                "CFM",
                modelName,
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );

        service.register(
                "CFM56-5B-2",
                "CFM",
                modelName.toLowerCase(),
                EngineType.TURBOFAN,
                new BigDecimal("122.00"),
                FuelType.JET_A1,
                new BigDecimal("37.70")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsNonPositivePower() {
        service.register(
                "CFM56-NEG",
                "CFM",
                "CFM56-NEG",
                EngineType.TURBOFAN,
                BigDecimal.ZERO,
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );
    }
}
