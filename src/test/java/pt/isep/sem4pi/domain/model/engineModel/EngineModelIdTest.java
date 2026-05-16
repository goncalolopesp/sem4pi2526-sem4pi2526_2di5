package pt.isep.sem4pi.domain.model.engineModel;

import org.junit.Test;

import static org.junit.Assert.*;

public class EngineModelIdTest {

    @Test
    public void constructorAndEquality() {
        final EngineModelId id = new EngineModelId("CFM56-5B");
        final EngineModelId same = new EngineModelId("CFM56-5B");
        final EngineModelId other = new EngineModelId("TRENT1000");

        assertEquals(id, same);
        assertNotEquals(id, other);
        assertEquals("CFM56-5B", id.toString());
        assertTrue(id.compareTo(other) != 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsBlankCode() {
        new EngineModelId("   ");
    }
}

