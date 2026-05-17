package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.Assert.*;

public class WeatherDataTest {

    @Test
    public void ensureValidWeatherDataIsCreated() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        Wind wind = new Wind(15.0, 180.0);
        Temperature temp = new Temperature(22.5);
        AtmosphericPressure pressure = new AtmosphericPressure(1015.0);
        Humidity humidity = new Humidity(55.0);
        Altitude altitude = new Altitude(150.0);

        WeatherData data = new WeatherData(date, time, wind, temp, pressure, humidity, altitude);

        assertNotNull(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfDateIsNull() {
        new WeatherData(null, LocalTime.now(), new Wind(15, 180), new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfTimeIsNull() {
        new WeatherData(LocalDate.now(), null, new Wind(15, 180), new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfAnyVOIsNull() {
        new WeatherData(LocalDate.now(), LocalTime.now(), null, new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }
}