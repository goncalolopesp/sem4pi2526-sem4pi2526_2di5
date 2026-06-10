package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;

import java.time.LocalDate;
import java.util.List;

public interface WeatherBatchRepository {
    WeatherImportBatch save(WeatherImportBatch batch);
    List<WeatherImportBatch> findAllByAreaId(String areaId);
    long countWeatherDataByAreaIdAndDate(String areaId, LocalDate date);
    List<WeatherData> findWeatherDataByAreaIdAndDate(String areaId, LocalDate date);
    long totalWeatherRecords();
}