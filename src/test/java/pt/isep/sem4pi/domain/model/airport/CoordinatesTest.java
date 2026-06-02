package pt.isep.sem4pi.domain.model.airport;


import org.junit.Test;

import static org.junit.Assert.*;

public class CoordinatesTest {

    @Test
    public void ensureValidCoordinatesAreAccepted() {
        Coordinates coords = new Coordinates(41.15, -8.61);
        assertEquals(41.15, coords.getLatitude(), 0.001);
        assertEquals(-8.61, coords.getLongitude(), 0.001);
    }

    @Test
    public void ensureLatitudeFailsIfTooHigh() {
        try {
            new Coordinates(91.0, 0.0);
            fail("Expected exception for invalid latitude");
        } catch (IllegalArgumentException e) {
            assertEquals("Latitude must be between -90 and 90 degrees.", e.getMessage());
        }
    }

    @Test
    public void ensureLongitudeFailsIfTooLow() {
        try {
            new Coordinates(0.0, -181.0);
            fail("Expected exception for invalid longitude");
        } catch (IllegalArgumentException e) {
            assertEquals("Longitude must be between -180 and 180 degrees.", e.getMessage());
        }
    }
}