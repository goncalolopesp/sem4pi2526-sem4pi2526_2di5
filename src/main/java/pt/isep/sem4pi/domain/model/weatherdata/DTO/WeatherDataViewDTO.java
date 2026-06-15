package pt.isep.sem4pi.domain.model.weatherdata.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class WeatherDataViewDTO {
    private final Long weatherDataId;
    private final String airControlAreaId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final double windSpeedKnots;
    private final double windDirectionDegrees;
    private final double temperatureCelsius;
    private final double pressureHpa;
    private final double humidityPercentage;
    private final double altitudeMeters;

    public WeatherDataViewDTO(String airControlAreaId, LocalDate date, LocalTime startTime, LocalTime endTime,
                              double windSpeedKnots, double windDirectionDegrees,
                              double temperatureCelsius, double pressureHpa,
                              double humidityPercentage, double altitudeMeters) {
        this(null, airControlAreaId, date, startTime, endTime, windSpeedKnots,
                windDirectionDegrees, temperatureCelsius, pressureHpa,
                humidityPercentage, altitudeMeters);
    }

    public WeatherDataViewDTO(Long weatherDataId, String airControlAreaId, LocalDate date,
                              LocalTime startTime, LocalTime endTime,
                              double windSpeedKnots, double windDirectionDegrees,
                              double temperatureCelsius, double pressureHpa,
                              double humidityPercentage, double altitudeMeters) {
        if (airControlAreaId == null || airControlAreaId.trim().isEmpty()) {
            throw new IllegalArgumentException("Air Control Area ID is required.");
        }
        if (date == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Date and time range are required.");
        }
        this.weatherDataId = weatherDataId;
        this.airControlAreaId = airControlAreaId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.windSpeedKnots = windSpeedKnots;
        this.windDirectionDegrees = windDirectionDegrees;
        this.temperatureCelsius = temperatureCelsius;
        this.pressureHpa = pressureHpa;
        this.humidityPercentage = humidityPercentage;
        this.altitudeMeters = altitudeMeters;
    }

    public Long getWeatherDataId() {
        return weatherDataId;
    }

    public String getAirControlAreaId() {
        return airControlAreaId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
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
}
