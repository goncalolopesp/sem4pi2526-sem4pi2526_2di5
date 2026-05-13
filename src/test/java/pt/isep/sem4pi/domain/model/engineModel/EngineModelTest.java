package pt.isep.sem4pi.domain.model.engineModel;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.Manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.Manufacture.ManufacturerId;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class EngineModelTest {

    @Test
    public void constructorAndIdentity() {
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

        assertEquals(new EngineModelId("CFM56-5B"), engineModel.identity());
        assertTrue(engineModel.hasManufacturerAndName(new ManufacturerId("CFM"), "CFM56-5B"));
        assertTrue(engineModel.hasManufacturerAndName(new ManufacturerId("CFM"), "cfm56-5b"));
        assertFalse(engineModel.hasManufacturerAndName(new ManufacturerId("CFM"), "OTHER"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyName() {
        final Manufacturer manufacturer = new Manufacturer(new ManufacturerId("CFM"), "CFM", "France");
        new EngineModel(
                new EngineModelId("CFM56-5B"),
                manufacturer,
                "   ",
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNonPositivePower() {
        final Manufacturer manufacturer = new Manufacturer(new ManufacturerId("CFM"), "CFM", "France");
        new EngineModel(
                new EngineModelId("CFM56-5B"),
                manufacturer,
                "CFM56-5B",
                EngineType.TURBOFAN,
                BigDecimal.ZERO,
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNonPositiveEfficiency() {
        final Manufacturer manufacturer = new Manufacturer(new ManufacturerId("CFM"), "CFM", "France");
        new EngineModel(
                new EngineModelId("CFM56-5B"),
                manufacturer,
                "CFM56-5B",
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                BigDecimal.ZERO
        );
    }
}

