package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;
import static org.junit.Assert.*;

/**
 * Test class for the AircraftModelId value object.
 * <p>This class contains unit tests for the AircraftModelId class,
 * verifying constructor validation, equality, hashing, and comparison methods.</p>
 */
public class AircraftModelIdTests {

    @Test
    public void testConstructorValid() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        String modelName = "A320";
        AircraftModelId id = new AircraftModelId(manId, modelName);
        assertEquals("AIRBUS - A320", id.toString());
    }

    @Test
    public void testConstructorNullManufacturerId() {
        try {
            new AircraftModelId(null, "A320");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullModelName() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        try {
            new AircraftModelId(manId, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorEmptyModelName() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        try {
            new AircraftModelId(manId, "");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorBlankModelName() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        try {
            new AircraftModelId(manId, "   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCompareToDifferentManufacturers() {
        ManufacturerId manId1 = new ManufacturerId("AIRBUS");
        ManufacturerId manId2 = new ManufacturerId("BOEING");
        AircraftModelId id1 = new AircraftModelId(manId1, "A320");
        AircraftModelId id2 = new AircraftModelId(manId2, "737");
        assertTrue(id1.compareTo(id2) < 0); // AIRBUS < BOEING
    }

    @Test
    public void testCompareToSameManufacturerDifferentModels() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A380");
        assertTrue(id1.compareTo(id2) < 0); // A320 < A380
    }

    @Test
    public void testCompareToEqual() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A320");
        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    public void testEqualsSameObject() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id = new AircraftModelId(manId, "A320");
        assertEquals(id, id);
    }

    @Test
    public void testEqualsDifferentValues() {
        ManufacturerId manId1 = new ManufacturerId("AIRBUS");
        ManufacturerId manId2 = new ManufacturerId("BOEING");
        AircraftModelId id1 = new AircraftModelId(manId1, "A320");
        AircraftModelId id2 = new AircraftModelId(manId2, "737");
        assertNotEquals(id1, id2);
    }

    @Test
    public void testEqualsSameValues() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A320");
        assertEquals(id1, id2);
    }

    @Test
    public void testHashCodeConsistency() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        AircraftModelId id1 = new AircraftModelId(manId, "A320");
        AircraftModelId id2 = new AircraftModelId(manId, "A320");
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void testToString() {
        ManufacturerId manId = new ManufacturerId("AIRBUS");
        String modelName = "A320";
        AircraftModelId id = new AircraftModelId(manId, modelName);
        assertEquals("AIRBUS - A320", id.toString());
    }
}
