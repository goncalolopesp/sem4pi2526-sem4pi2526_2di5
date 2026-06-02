package pt.isep.sem4pi.application.service.weatherdata;

import pt.isep.sem4pi.domain.model.weatherdata.*;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegisterWeatherService {

    private final WeatherBatchRepository repository;

    public RegisterWeatherService(WeatherBatchRepository repository) {
        this.repository = repository;
    }

    public WeatherImportBatch registerManualData(String areaId, double tempC, double windSpeed, double windDir,
                                                 double humidityPct, double pressureHpa, double altitudeM) {
        Temperature temp = new Temperature(tempC);
        Wind wind = new Wind(windSpeed, windDir);
        Humidity humidity = new Humidity(humidityPct);
        AtmosphericPressure pressure = new AtmosphericPressure(pressureHpa);
        Altitude altitude = new Altitude(altitudeM);

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        WeatherData data = new WeatherData(date, time, time, wind, temp, pressure, humidity, altitude);

        WeatherImportBatch batch = new WeatherImportBatch(areaId, data);

        return repository.save(batch);
    }
}