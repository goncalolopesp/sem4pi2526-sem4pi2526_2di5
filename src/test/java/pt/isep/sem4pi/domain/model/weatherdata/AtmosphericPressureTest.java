package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;
import static org.junit.Assert.*;

public class AtmosphericPressureTest {

    @Test
    public void ensureValidPressure() {
        AtmosphericPressure pressure = new AtmosphericPressure(1013.25);
        assertEquals(1013.25, pressure.getHPa(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensurePressureFailsIfTooLow() {
        new AtmosphericPressure(700.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensurePressureFailsIfTooHigh() {
        new AtmosphericPressure(1200.0);
    }
}