package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.representations.RepresentationBuilder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the AircraftModel aggregate.
 */
public class AircraftModelTests {

    private AircraftModelId aircraftModelId;
    private ManufacturerId manufacturerId;
    private ManufacturerId engineManufacturerId;
    private Manufacturer engineManufacturer;
    private AircraftType aircraftType;
    private EngineType motorization;
    private Weight weightLimits;
    private Coefficient aerodynamics;
    private AircraftPerformance performance;
    private Fuel fuel;
    private WingGeometry wingGeometry;
    private List<EngineModel> engineModels;
    private Integer maximumCapacity;

    @Before
    public void setUp() {
        manufacturerId = new ManufacturerId("AIRBUS");
        aircraftModelId = new AircraftModelId(manufacturerId, "A320");
        aircraftType = AircraftType.PASSENGER;
        motorization = EngineType.TURBOFAN;

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

        maximumCapacity = 180;

        engineManufacturerId = new ManufacturerId("CFM");
        engineManufacturer = mock(Manufacturer.class);
        when(engineManufacturer.identity()).thenReturn(engineManufacturerId);

        engineModels = new ArrayList<>();
        engineModels.add(buildEngine("CFM56-5B4", EngineType.TURBOFAN));
    }

    private EngineModel buildEngine(String code, EngineType type) {
        return new EngineModel(
                new EngineModelId(code),
                engineManufacturer,
                code,
                type,
                new BigDecimal("10000"),
                FuelType.JET_A1,
                new BigDecimal("0.80")
        );
    }

    private AircraftModel buildAircraftModel() {
        return new AircraftModel(
                aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, maximumCapacity
        );
    }

    @Test
    public void testProtectedJPAConstructor() throws Exception {
        // Usa Reflection para aceder ao construtor protegido exigido pelo JPA
        Constructor<AircraftModel> constructor = AircraftModel.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AircraftModel am = constructor.newInstance();
        assertNotNull(am);
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
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAircraftType() {
        new AircraftModel(aircraftModelId, null, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullMotorization() {
        new AircraftModel(aircraftModelId, aircraftType, null,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWeightLimits() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                null, aerodynamics, performance, fuel, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAerodynamics() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, null, performance, fuel, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPerformance() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, null, fuel, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFuel() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, null, wingGeometry, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullWingGeometry() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, null, engineModels, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullEngineModels() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, null, maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyEngineModels() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry,
                new ArrayList<>(), maximumCapacity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullMaximumCapacity() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMaximumCapacity() {
        new AircraftModel(aircraftModelId, aircraftType, motorization,
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, -10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCertifyEngineNullEngine() {
        AircraftModel am = buildAircraftModel();
        am.certifyEngine(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCertifyEngineDuplicateEngine() {
        AircraftModel am = buildAircraftModel();
        EngineModel duplicate = buildEngine("CFM56-5B4", EngineType.TURBOFAN);
        am.certifyEngine(duplicate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCertifyEngineIncompatibleEngineType() {
        AircraftModel am = buildAircraftModel();
        EngineModel incompatibleEngine = buildEngine("PW150", EngineType.TURBOPROP);
        am.certifyEngine(incompatibleEngine);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCertifyEngineWithNullTypeInEngine() {
        AircraftModel am = buildAircraftModel();
        EngineModel mockEngine = mock(EngineModel.class);
        when(mockEngine.type()).thenReturn(null);
        am.certifyEngine(mockEngine);
    }

    @Test
    public void testCertifyEngineNewEngine() {
        AircraftModel am = buildAircraftModel();
        EngineModel newEngine = buildEngine("CFM56-5B5", EngineType.TURBOFAN);
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
                weightLimits, aerodynamics, performance, fuel, wingGeometry, engineModels, maximumCapacity
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

    @Test
    public void testGettersAndToString() {
        AircraftModel am = buildAircraftModel();

        assertNotNull(am.modelName());
        assertEquals(AircraftType.PASSENGER, am.aircraftType());
        assertEquals(EngineType.TURBOFAN, am.motorization());
        assertEquals(fuel, am.fuel());
        assertEquals(wingGeometry, am.wingGeometry());
        assertEquals(aerodynamics, am.aerodynamics());
        assertEquals(performance, am.performance());
        assertEquals(weightLimits, am.weightLimits());
        assertEquals(1, am.certifiedEngines().size());
        assertNull(am.version());

        String toStringResult = am.toString();
        assertTrue(toStringResult.contains("AircraftModel"));
        assertTrue(toStringResult.contains("A320"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBuildRepresentation() {
        AircraftModel am = buildAircraftModel();

        RepresentationBuilder<Object> mockBuilder = mock(RepresentationBuilder.class, RETURNS_DEEP_STUBS);

        am.buildRepresentation(mockBuilder);

        verify(mockBuilder).startObject("AircraftModel");
    }
}