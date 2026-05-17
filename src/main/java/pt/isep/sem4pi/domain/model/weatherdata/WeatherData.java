package pt.isep.sem4pi.domain.model.weatherdata;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "WeatherData")
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weatherDataId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Embedded private Wind wind;
    @Embedded private Temperature temperature;
    @Embedded private AtmosphericPressure pressure;
    @Embedded private Humidity humidity;
    @Embedded private Altitude altitude;

    protected WeatherData() {}

    public WeatherData(LocalDate date, LocalTime time, Wind wind, Temperature temperature,
                       AtmosphericPressure pressure, Humidity humidity, Altitude altitude) {
        if (date == null || time == null || wind == null || temperature == null ||
                pressure == null || humidity == null || altitude == null) {
            throw new IllegalArgumentException("All weather metrics and timestamps must be provided.");
        }
        this.date = date;
        this.time = time;
        this.wind = wind;
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.altitude = altitude;
    }
}