package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the AircraftModelId value object.
 * <p>This class contains unit tests for the AircraftModelId class,
 * verifying constructor validation, equality, hashing, and comparison methods.</p>
 */
class AircraftModelIdTests {

    @Test
    void testConstructorValid() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        String modelName = "A320";
        AircraftModelId id = new AircraftModelId(manId, modelName);
        assertEquals("AIRBUS - A320", id.toString());
    }

    @Test
    void testConstructorNullManufacturerId() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId(null, "A320"));
    }

    @Test
    void testConstructorNullModelName() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId(manId, null));
    }

    @Test
    void testConstructorEmptyModelName() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId(manId, ""));
    }

    @Test
    void testConstructorBlankModelName() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId(manId, "   "));
    }

    @Test
    void testCompareToDifferentManufacturers() {
        ManufacturerId manId1 = new ManufacturerId("AIRBUS");
        ManufacturerId manId2 = new ManufacturerId("BOEING");
        AircraftModelId id1 = new AircraftModelId(manId1, "A320");
        AircraftModelId id2 = new AircraftModelId(manId2, "737");
        assertTrue(id1.compareTo(id2) < 0); // AIRBUS < BOEING
    }

    @Test
    void testCompareToSameManufacturerDifferentModels() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A380");
        assertTrue(id1.compareTo(id2) < 0); // A320 < A380
    }

    @Test
    void testCompareToEqual() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A320");
        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    void testEqualsSameObject() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id = new AircraftModelId(manId, "A320");
        assertEquals(id, id);
    }

    @Test
    void testEqualsDifferentValues() {
        ManufacturerId manId1 = new ManufacturerId("AIRBUS");
        ManufacturerId manId2 = new ManufacturerId("BOEING");
        AircraftModelId id1 = new AircraftModelId(manId1, "A320");
        AircraftModelId id2 = new AircraftModelId(manId2, "737");
        assertNotEquals(id1, id2);
    }

    @Test
    void testEqualsSameValues() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A320");
        assertEquals(id1, id2);
    }

    @Test
    void testHashCodeConsistency() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A320");
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testToString() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        String modelName = "A320";
        AircraftModelId id = new AircraftModelId(manId, modelName);
        assertEquals("AIRBUS - A320", id.toString());
    }
}
