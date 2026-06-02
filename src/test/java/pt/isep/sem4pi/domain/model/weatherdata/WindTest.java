package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;

import static org.junit.Assert.*;

public class WindTest {

    @Test
    public void ensureValidWindIsCreated() {
        Wind wind = new Wind(15.0, 180.0);
        assertEquals(15.0, wind.getSpeedKnots(), 0.001);
        assertEquals(180.0, wind.getDirectionDegrees(), 0.001);
    }

    @Test
    public void ensureWindFailsIfNegativeSpeed() {
        try {
            new Wind(-5.0, 180.0);
            fail("Expected exception for negative speed");
        } catch (IllegalArgumentException e) {
            assertEquals("Wind speed cannot be negative.", e.getMessage());
        }
    }

    @Test
    public void ensureWindFailsIfInvalidDirection() {
        try {
            new Wind(10.0, 360.0);
            fail("Expected exception for invalid direction");
        } catch (IllegalArgumentException e) {
            assertEquals("Wind direction must be 0-359 degrees.", e.getMessage());
        }
    }
}