package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;
import pt.isep.sem4pi.domain.weatherdata.*;

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

        WeatherData data = new WeatherData(date, time, time, wind, temp, pressure, humidity, altitude);

        assertNotNull(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfDateIsNull() {
        LocalTime time = LocalTime.now();
        new WeatherData(null, time, time, new Wind(15, 180), new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfTimeIsNull() {
        new WeatherData(LocalDate.now(), null, LocalTime.now(), new Wind(15, 180), new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfAnyVOIsNull() {
        LocalTime time = LocalTime.now();
        new WeatherData(LocalDate.now(), time, time, null, new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfEndBeforeStart() {
        new WeatherData(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(9, 0), new Wind(15, 180), new Temperature(22.5),
                new AtmosphericPressure(1015), new Humidity(55), new Altitude(150));
    }
}