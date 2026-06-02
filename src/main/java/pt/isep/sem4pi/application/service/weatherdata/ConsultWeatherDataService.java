package pt.isep.sem4pi.application.service.weatherdata;

import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultWeatherDataService {

    private final WeatherBatchRepository repository;

    public ConsultWeatherDataService(WeatherBatchRepository repository) {
        this.repository = repository;
    }

    public List<WeatherDataViewDTO> findByAreaAndDate(String areaId, LocalDate date) {
        if (areaId == null || areaId.trim().isEmpty()) {
            throw new IllegalArgumentException("Air Control Area ID is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        List<WeatherImportBatch> batches = repository.findAllByAreaId(areaId.trim());
        return batches.stream()
                .flatMap(batch -> batch.getRecords().stream()
                        .filter(record -> date.equals(record.getDate()))
                        .map(record -> toViewDTO(batch.getAirControlAreaId(), record)))
                .sorted(Comparator.comparing(WeatherDataViewDTO::getStartTime)
                        .thenComparing(WeatherDataViewDTO::getEndTime))
                .collect(Collectors.toList());
    }

    private WeatherDataViewDTO toViewDTO(String areaId, WeatherData record) {
        return new WeatherDataViewDTO(
                areaId,
                record.getDate(),
                record.getStartTime(),
                record.getEndTime(),
                record.getWind().getSpeedKnots(),
                record.getWind().getDirectionDegrees(),
                record.getTemperature().getCelsius(),
                record.getPressure().getHPa(),
                record.getHumidity().getPercentage(),
                record.getAltitude().getMeters()
        );
    }
}
