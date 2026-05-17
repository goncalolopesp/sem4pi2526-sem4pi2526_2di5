package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;
import static org.junit.Assert.*;

public class HumidityTest {

    @Test
    public void ensureValidHumidity() {
        Humidity humidity = new Humidity(55.0);
        assertEquals(55.0, humidity.getPercentage(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureHumidityFailsIfNegative() {
        new Humidity(-1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureHumidityFailsIfOver100() {
        new Humidity(105.0);
    }
}