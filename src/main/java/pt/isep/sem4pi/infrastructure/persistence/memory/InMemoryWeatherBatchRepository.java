package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryWeatherBatchRepository implements WeatherBatchRepository {

    private final List<WeatherImportBatch> database = new ArrayList<>();

    @Override
    public WeatherImportBatch save(WeatherImportBatch batch) {
        database.add(batch);
        return batch;
    }

    @Override
    public List<WeatherImportBatch> findAllByAreaId(String areaId) {
        return database.stream()
                .filter(b -> b.getAirControlAreaId().equals(areaId))
                .collect(Collectors.toList());
    }

    @Override
    public long totalWeatherRecords() {
        return database.stream()
                .mapToLong(WeatherImportBatch::getImportedRecords)
                .sum();
    }
}