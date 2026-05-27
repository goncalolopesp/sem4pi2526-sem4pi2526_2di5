package pt.isep.sem4pi.domain.model.weatherdata.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class WeatherDataDTO {
    private final String airControlAreaId;
    private final LocalDate date;
    private final LocalTime time;
    private final double windSpeedKnots;
    private final double windDirectionDegrees;
    private final double temperatureCelsius;
    private final double pressureHpa;
    private final double humidityPercentage;
    private final double altitudeMeters;
    private final int sourceLine;

    public WeatherDataDTO(String airControlAreaId, LocalDate date, LocalTime time,
                          double windSpeedKnots, double windDirectionDegrees,
                          double temperatureCelsius, double pressureHpa,
                          double humidityPercentage, double altitudeMeters,
                          int sourceLine) {
        this.airControlAreaId = airControlAreaId;
        this.date = date;
        this.time = time;
        this.windSpeedKnots = windSpeedKnots;
        this.windDirectionDegrees = windDirectionDegrees;
        this.temperatureCelsius = temperatureCelsius;
        this.pressureHpa = pressureHpa;
        this.humidityPercentage = humidityPercentage;
        this.altitudeMeters = altitudeMeters;
        this.sourceLine = sourceLine;
    }

    public String getAirControlAreaId() {
        return airControlAreaId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public double getWindSpeedKnots() {
        return windSpeedKnots;
    }

    public double getWindDirectionDegrees() {
        return windDirectionDegrees;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getPressureHpa() {
        return pressureHpa;
    }

    public double getHumidityPercentage() {
        return humidityPercentage;
    }

    public double getAltitudeMeters() {
        return altitudeMeters;
    }

    public int getSourceLine() {
        return sourceLine;
    }
}

