package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for the AircraftModel aggregate.
 * <p>This class contains comprehensive unit tests for the AircraftModel class,
 * including constructor validation, engine certification, identity comparison,
 * and various edge cases for all parameters.</p>
 */
public class AircraftModelTests {

    private AircraftModelId aircraftModelId;
    private ManufacturerId manufacturerId;
    private AircraftType aircraftType;
    private String modelName;
    private MotorizationType motorization;
    private Weight weightLimits;
    private Coefficient aerodynamics;
    private AircraftPerformance performance;
    private Fuel fuel;
    private WingGeometry wingGeometry;
    private List<EngineModelId> engineModelIds;

    @Before
    public void setUp() {
        manufacturerId = new ManufacturerId("AIRBUS");
        aircraftModelId = new AircraftModelId(manufacturerId, "A320");
        aircraftType = AircraftType.PASSENGER;
        modelName = "Airbus A320";
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

        engineModelIds = new ArrayList<>();
        engineModelIds.add(new EngineModelId("CFM56-5B4"));
    }

    @Test
    public void testConstructorValidValues() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertEquals(aircraftModelId, am.identity());
        assertNotNull(am);
    }

    @Test
    public void testConstructorNullAircraftModelId() {
        try {
            new AircraftModel(null, aircraftType, motorization,
                    weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullAircraftType() {
        try {
            new AircraftModel(aircraftModelId, null, motorization,
                    weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullMotorization() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, null,
                    weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullWeightLimits() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    null, aerodynamics, performance, fuel, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullAerodynamics() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    weightLimits, null, performance, fuel, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullPerformance() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    weightLimits, aerodynamics, null, fuel, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullFuel() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    weightLimits, aerodynamics, performance, null, wingGeometry, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullWingGeometry() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    weightLimits, aerodynamics, performance, fuel, null, engineModelIds);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullEngineModelIds() {
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    weightLimits, aerodynamics, performance, fuel, wingGeometry, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorEmptyEngineModelIds() {
        List<EngineModelId> emptyEngines = new ArrayList<>();
        try {
            new AircraftModel(aircraftModelId, aircraftType, motorization,
                    weightLimits, aerodynamics, performance, fuel, wingGeometry, emptyEngines);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCertifyEngineNullEngineId() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );
        try {
            am.certifyEngine(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCertifyEngineDuplicateEngine() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        EngineModelId duplicateEngine = engineModelIds.get(0);
        try {
            am.certifyEngine(duplicateEngine);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCertifyEngineNewEngine() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        EngineModelId newEngine = new EngineModelId("CFM56-5B5");
        try {
            am.certifyEngine(newEngine);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage());
        }
    }

    @Test
    public void testSameAsIdentity() {
        AircraftModel am1 = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        AircraftModel am2 = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertTrue(am1.sameAs(am2));
    }

    @Test
    public void testSameAsNotIdentity() {
        AircraftModel am1 = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        AircraftModelId differentId = new AircraftModelId(manufacturerId, "B737");
        AircraftModel am2 = new AircraftModel(
                differentId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertFalse(am1.sameAs(am2));
    }

    @Test
    public void testSameAsNotAircraftModel() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertFalse(am.sameAs("NotAnAircraftModel"));
    }

    @Test
    public void testIdentity() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertEquals(aircraftModelId, am.identity());
    }
}
