package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.application.service.weatherdata.RegisterWeatherService;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class RegisterWeatherController {
    private final RegisterWeatherService service;

    public RegisterWeatherController() {
        this(PersistenceContext.repositories().weatherBatches());
    }

    public RegisterWeatherController(WeatherBatchRepository repository) {
        this.service = new RegisterWeatherService(repository);
    }

    public WeatherImportBatch registerWeather(String areaId, double temp, double windSpeed, double windDir,
                                              double humidity, double pressure, double altitude) {
        return service.registerManualData(areaId, temp, windSpeed, windDir, humidity, pressure, altitude);
    }
}