package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;

import static org.junit.Assert.*;

public class TemperatureTest {

    @Test
    public void ensureValidTemperature() {
        Temperature temp = new Temperature(22.5);
        assertEquals(22.5, temp.getCelsius(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTemperatureFailsIfTooHot() {
        new Temperature(70.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTemperatureFailsIfTooCold() {
        new Temperature(-100.0);
    }
}