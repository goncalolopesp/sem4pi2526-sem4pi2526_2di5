package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the AircraftModel aggregate.
 * <p>This class contains comprehensive unit tests for the AircraftModel class,
 * including constructor validation, engine certification, identity comparison,
 * and various edge cases for all parameters.</p>
 */
class AircraftModelTests {

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

    @BeforeEach
    void setUp() {
        aircraftModelId = new AircraftModelId("A320");
        manufacturerId = new ManufacturerId("AIRBUS");
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
    void testConstructorValidValues() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertEquals(aircraftModelId, am.identity());
        assertNotNull(am);
    }

    @Test
    void testConstructorNullAircraftModelId() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(null, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullManufacturerId() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, null, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullAircraftType() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, null, modelName, motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullModelName() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, null, motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorEmptyModelName() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, "", motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorBlankModelName() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, "   ", motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullMotorization() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, null,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullWeightLimits() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        null, aerodynamics, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullAerodynamics() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, null, performance, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullPerformance() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, null, fuel, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullFuel() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, performance, null, wingGeometry, engineModelIds)
        );
    }

    @Test
    void testConstructorNullWingGeometry() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, performance, fuel, null, engineModelIds)
        );
    }

    @Test
    void testConstructorNullEngineModelIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, null)
        );
    }

    @Test
    void testConstructorEmptyEngineModelIds() {
        List<EngineModelId> emptyEngines = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                        weightLimits, aerodynamics, performance, fuel, wingGeometry, emptyEngines)
        );
    }

    @Test
    void testCertifyEngineNullEngineId() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertThrows(IllegalArgumentException.class, () -> am.certifyEngine(null));
    }

    @Test
    void testCertifyEngineDuplicateEngine() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        EngineModelId duplicateEngine = engineModelIds.get(0);
        assertThrows(IllegalArgumentException.class, () -> am.certifyEngine(duplicateEngine));
    }

    @Test
    void testCertifyEngineNewEngine() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        EngineModelId newEngine = new EngineModelId("CFM56-5B5");
        assertDoesNotThrow(() -> am.certifyEngine(newEngine));
    }

    @Test
    void testSameAsIdentity() {
        AircraftModel am1 = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        AircraftModel am2 = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertTrue(am1.sameAs(am2));
    }

    @Test
    void testSameAsNotIdentity() {
        AircraftModel am1 = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        AircraftModelId differentId = new AircraftModelId("B737");
        AircraftModel am2 = new AircraftModel(
                differentId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertFalse(am1.sameAs(am2));
    }

    @Test
    void testSameAsNotAircraftModel() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertFalse(am.sameAs("NotAnAircraftModel"));
    }

    @Test
    void testIdentity() {
        AircraftModel am = new AircraftModel(
                aircraftModelId, manufacturerId, aircraftType, modelName, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModelIds
        );

        assertEquals(aircraftModelId, am.identity());
    }
}
