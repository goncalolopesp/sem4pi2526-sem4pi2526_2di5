package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the AircraftModel aggregate.
 */
public class AircraftModelTests {

    private AircraftModelId aircraftModelId;
    private ManufacturerId manufacturerId;
    private ManufacturerId engineManufacturerId;
    private Manufacturer engineManufacturer;
    private AircraftType aircraftType;
    private MotorizationType motorization;
    private Weight weightLimits;
    private Coefficient aerodynamics;
    private AircraftPerformance performance;
    private Fuel fuel;
    private WingGeometry wingGeometry;
    private List<EngineModel> engineModels;

    @Before
    public void setUp() {
        manufacturerId = new ManufacturerId("AIRBUS");
        aircraftModelId = new AircraftModelId(manufacturerId, "A320");
        aircraftType = AircraftType.PASSENGER;
        motorization = MotorizationType.TURBOFAN;

        weightLimits = new Weight(
                new BigDecimal("68000.00"),
                new BigDecimal("77000.00"),
                new BigDecimal("89000.00")
        );
        aerodynamics = new Coefficient(
                new BigDecimal("0.024"),
                new BigDecimal("0.15")
        );
        performance = new AircraftPerformance(
                new BigDecimal("35000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("5000.00")
        );
        fuel = new Fuel(new BigDecimal("27200.00"));
        wingGeometry = new WingGeometry(
                new BigDecimal("122.40"),
                new BigDecimal("9.45")
        );

        // Mock manufacturer of the engine (CFM)
        engineManufacturerId = new ManufacturerId("CFM");
        engineManufacturer = mock(Manufacturer.class);
        when(engineManufacturer.identity()).thenReturn(engineManufacturerId);

        // Build a real EngineModel (the AircraftModel now expects full objects)
        engineModels = new ArrayList<>();
        engineModels.add(buildEngine("CFM56-5B4"));
    }

    private EngineModel buildEngine(String code) {
        return new EngineModel(
                new EngineModelId(code),
                engineManufacturer,
                code,
                EngineType.TURBOFAN,
                new BigDecimal("10000"),
                FuelType.JET_A1,
                new BigDecimal("0.80")
        );
    }

    private AircraftModel buildAircraftModel() {
        return new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels
        );
    }

    @Test
    public void testConstructorValidValues() {
        AircraftModel am = buildAircraftModel();

        assertNotNull(am);
        assertEquals(aircraftModelId, am.identity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAircraftModelId() {
        new AircraftModel(null, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAircraftType() {
        new AircraftModel(aircraftModelId, null, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullMotorization() {
        new AircraftModel(aircraftModelId, aircraftType, null,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWeightLimits() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                null, aerodynamics, performance, fuel, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAerodynamics() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, null, performance, fuel, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPerformance() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, null, fuel, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFuel() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, null, wingGeometry, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWingGeometry() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, null, engineModels);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullEngineModels() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyEngineModels() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry,
                new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCertifyEngineNullEngine() {
        AircraftModel am = buildAircraftModel();
        am.certifyEngine(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCertifyEngineDuplicateEngine() {
        AircraftModel am = buildAircraftModel();
        // Same engine already in the constructor
        EngineModel duplicate = buildEngine("CFM56-5B4");
        am.certifyEngine(duplicate);
    }

    @Test
    public void testCertifyEngineNewEngine() {
        AircraftModel am = buildAircraftModel();
        EngineModel newEngine = buildEngine("CFM56-5B5");

        try {
            am.certifyEngine(newEngine);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage());
        }
    }

    @Test
    public void testSameAsIdentity() {
        AircraftModel am1 = buildAircraftModel();
        AircraftModel am2 = buildAircraftModel();

        assertTrue(am1.sameAs(am2));
    }

    @Test
    public void testSameAsNotIdentity() {
        AircraftModel am1 = buildAircraftModel();

        AircraftModelId differentId = new AircraftModelId(manufacturerId, "B737");
        AircraftModel am2 = new AircraftModel(
                differentId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels
        );

        assertFalse(am1.sameAs(am2));
    }

    @Test
    public void testSameAsNotAircraftModel() {
        AircraftModel am = buildAircraftModel();
        assertFalse(am.sameAs("NotAnAircraftModel"));
    }

    @Test
    public void testIdentity() {
        AircraftModel am = buildAircraftModel();
        assertEquals(aircraftModelId, am.identity());
    }
}