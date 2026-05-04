package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AircraftModelIdTests {

    @Test
    void testConstructorValidCode() {
        String code = "A320";
        AircraftModelId id = new AircraftModelId(code);
        assertEquals(code, id.code());
    }

    @Test
    void testConstructorNullCode() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId(null));
    }

    @Test
    void testConstructorEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId(""));
    }

    @Test
    void testConstructorBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelId("   "));
    }

    @Test
    void testCompareTo() {
        AircraftModelId id1 = new AircraftModelId("A320");
        AircraftModelId id2 = new AircraftModelId("B737");
        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertEquals(0, id1.compareTo(new AircraftModelId("A320")));
    }

    @Test
    void testEqualsSameObject() {
        AircraftModelId id = new AircraftModelId("A320");
        assertEquals(id, id);
    }

    @Test
    void testEqualsDifferentValues() {
        AircraftModelId id1 = new AircraftModelId("A320");
        AircraftModelId id2 = new AircraftModelId("B737");
        assertNotEquals(id1, id2);
    }

    @Test
    void testEqualsSameValues() {
        AircraftModelId id1 = new AircraftModelId("A320");
        AircraftModelId id2 = new AircraftModelId("A320");
        assertEquals(id1, id2);
    }

    @Test
    void testHashCodeConsistency() {
        AircraftModelId id1 = new AircraftModelId("A320");
        AircraftModelId id2 = new AircraftModelId("A320");
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testToString() {
        String code = "A320";
        AircraftModelId id = new AircraftModelId(code);
        assertEquals(code, id.toString());
    }
}

