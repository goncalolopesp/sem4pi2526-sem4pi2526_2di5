package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface WeatherBatchRepository {
    WeatherImportBatch save(WeatherImportBatch batch);
    List<WeatherImportBatch> findAll();

    List<WeatherData> findWeatherDataByAreaIdAndDate(String areaId, LocalDate date);

    List<WeatherImportBatch> findAllByAreaId(String areaId);
    long countWeatherDataByAreaIdAndDate(String areaId, LocalDate date);
    Optional<WeatherData> findWeatherDataById(Long weatherDataId);
    Optional<WeatherData> findWeatherData(String areaId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime);
    long totalWeatherRecords();
}
