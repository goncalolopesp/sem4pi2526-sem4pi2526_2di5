package pt.isep.sem4pi.application.service.weatherdata;

import pt.isep.sem4pi.domain.model.weatherdata.Altitude;
import pt.isep.sem4pi.domain.model.weatherdata.AtmosphericPressure;
import pt.isep.sem4pi.domain.model.weatherdata.Humidity;
import pt.isep.sem4pi.domain.model.weatherdata.Temperature;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherTimeFormatter;
import pt.isep.sem4pi.domain.model.weatherdata.Wind;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegisterWeatherService {

    private final WeatherBatchRepository repository;

    public RegisterWeatherService(WeatherBatchRepository repository) {
        this.repository = repository;
    }

    public WeatherImportBatch registerManualData(String areaId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                                 double tempC, double windSpeed, double windDir, double humidityPct,
                                                 double pressureHpa, double altitudeM) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end time are required.");
        }
        Temperature temp = new Temperature(tempC);
        Wind wind = new Wind(windSpeed, windDir);
        Humidity humidity = new Humidity(humidityPct);
        AtmosphericPressure pressure = new AtmosphericPressure(pressureHpa);
        Altitude altitude = new Altitude(altitudeM);

        final LocalTime normalizedStart = WeatherTimeFormatter.normalize(startTime);
        final LocalTime normalizedEnd = WeatherTimeFormatter.normalize(endTime);

        WeatherData data = new WeatherData(date, normalizedStart, normalizedEnd, wind, temp, pressure, humidity, altitude);

        WeatherImportBatch batch = new WeatherImportBatch(areaId, data);

        return repository.save(batch);
    }
}