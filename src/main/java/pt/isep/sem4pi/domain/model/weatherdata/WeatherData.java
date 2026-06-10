package pt.isep.sem4pi.domain.model.weatherdata;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.TIME)
    private LocalTime startTime;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.TIME)
    private LocalTime endTime;

    @Embedded private Wind wind;
    @Embedded private Temperature temperature;
    @Embedded private AtmosphericPressure pressure;
    @Embedded private Humidity humidity;
    @Embedded private Altitude altitude;

    protected WeatherData() {}

    public WeatherData(LocalDate date, LocalTime startTime, LocalTime endTime, Wind wind, Temperature temperature,
                       AtmosphericPressure pressure, Humidity humidity, Altitude altitude) {
        if (date == null || startTime == null || endTime == null || wind == null || temperature == null ||
                pressure == null || humidity == null || altitude == null) {
            throw new IllegalArgumentException("All weather metrics and timestamps must be provided.");
        }
        final LocalTime normalizedStart = WeatherTimeFormatter.normalize(startTime);
        final LocalTime normalizedEnd = WeatherTimeFormatter.normalize(endTime);
        if (normalizedEnd.isBefore(normalizedStart)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.date = date;
        this.startTime = normalizedStart;
        this.endTime = normalizedEnd;
        this.wind = wind;
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.altitude = altitude;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getWeatherDataId() {
        return weatherDataId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Wind getWind() {
        return wind;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public AtmosphericPressure getPressure() {
        return pressure;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public Altitude getAltitude() {
        return altitude;
    }
}
