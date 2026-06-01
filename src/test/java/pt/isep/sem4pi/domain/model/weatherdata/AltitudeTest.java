package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;
import pt.isep.sem4pi.domain.weatherdata.Altitude;

import static org.junit.Assert.*;

public class AltitudeTest {

    @Test
    public void ensureValidAltitudeIsAccepted() {
        Altitude alt = new Altitude(150.0);
        assertEquals(150.0, alt.getMeters(), 0.001);
    }

    @Test
    public void ensureAltitudeFailsIfTooLow() {
        try {
            new Altitude(-600.0);
            fail("Expected exception for low altitude");
        } catch (IllegalArgumentException e) {
            assertEquals("Altitude must be a valid atmospheric height.", e.getMessage());
        }
    }

    @Test
    public void ensureAltitudeFailsIfTooHigh() {
        try {
            new Altitude(60000.0);
            fail("Expected exception for high altitude");
        } catch (IllegalArgumentException e) {
            assertEquals("Altitude must be a valid atmospheric height.", e.getMessage());
        }
    }
}