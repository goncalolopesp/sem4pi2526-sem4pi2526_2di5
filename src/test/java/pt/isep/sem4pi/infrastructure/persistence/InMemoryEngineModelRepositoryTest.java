package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryEngineModelRepository;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class InMemoryEngineModelRepositoryTest {

    private InMemoryEngineModelRepository repo;

    @Before
    public void setUp() {
        repo = new InMemoryEngineModelRepository();
    }

    @Test
    public void existsWithManufacturerAndNameMatchesCaseInsensitive() {
        final Manufacturer manufacturer = new Manufacturer(new ManufacturerId("CFM"), "CFM", "France");
        final EngineModel engineModel = new EngineModel(
                new EngineModelId("CFM56-5B"),
                manufacturer,
                "CFM56-5B",
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );

        repo.save(engineModel);

        assertTrue(repo.existsWithManufacturerAndName(new ManufacturerId("CFM"), "CFM56-5B"));
        assertTrue(repo.existsWithManufacturerAndName(new ManufacturerId("CFM"), "cfm56-5b"));
        assertFalse(repo.existsWithManufacturerAndName(new ManufacturerId("RR"), "CFM56-5B"));
    }
}

