package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;

import java.util.List;

public interface WeatherBatchRepository {
    WeatherImportBatch save(WeatherImportBatch batch);
    List<WeatherImportBatch> findAllByAreaId(String areaId);
}