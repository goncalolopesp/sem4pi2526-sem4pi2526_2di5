package pt.isep.sem4pi.application.controller.weatherdata;

import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class WeatherDataTotalsController {
    private final WeatherBatchRepository repository;

    public WeatherDataTotalsController() {
        this.repository = PersistenceContext.repositories().weatherBatches();
    }

    public long totalWeatherRecords() {
        return repository.totalWeatherRecords();
    }
}

