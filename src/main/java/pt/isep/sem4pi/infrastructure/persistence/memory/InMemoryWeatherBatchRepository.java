package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
    public long countWeatherDataByAreaIdAndDate(final String areaId, final LocalDate date) {
        return findWeatherDataByAreaIdAndDate(areaId, date).size();
    }

    @Override
    public List<WeatherData> findWeatherDataByAreaIdAndDate(final String areaId, final LocalDate date) {
        return database.stream()
                .filter(b -> b.getAirControlAreaId().equals(areaId))
                .flatMap(b -> b.getRecords().stream())
                .filter(record -> date.equals(record.getDate()))
                .sorted(Comparator.comparing(WeatherData::getStartTime).thenComparing(WeatherData::getEndTime))
                .collect(Collectors.toList());
    }

    @Override
    public long totalWeatherRecords() {
        return database.stream()
                .mapToLong(WeatherImportBatch::getImportedRecords)
                .sum();
    }
}