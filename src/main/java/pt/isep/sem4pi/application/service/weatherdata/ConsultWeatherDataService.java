package pt.isep.sem4pi.application.service.weatherdata;

import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherTimeFormatter;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the US043 weather-data query.
 *
 * <p>The filtering is delegated to {@link WeatherBatchRepository}, allowing
 * the JPA implementation to execute the query in the database instead of
 * loading every import batch into memory. Domain entities are mapped to view
 * DTOs before crossing into the presentation layer.</p>
 */
public class ConsultWeatherDataService {

    private final WeatherBatchRepository repository;

    public ConsultWeatherDataService(WeatherBatchRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds all measurements belonging to an area on the requested date.
     *
     * <p>The preliminary count avoids retrieving and mapping records when no
     * match exists. Times are normalized before presentation to remove
     * irrelevant seconds/nanoseconds introduced by external file formats.</p>
     *
     * @param areaId Air Control Area identifier
     * @param date requested day
     * @return matching measurements represented as presentation DTOs
     * @throws IllegalArgumentException when the area or date is missing
     */
    public List<WeatherDataViewDTO> findByAreaAndDate(String areaId, LocalDate date) {
        if (areaId == null || areaId.trim().isEmpty()) {
            throw new IllegalArgumentException("Air Control Area ID is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        final String normalizedAreaId = areaId.trim();
        if (repository.countWeatherDataByAreaIdAndDate(normalizedAreaId, date) == 0) {
            return List.of();
        }
        return repository.findWeatherDataByAreaIdAndDate(normalizedAreaId, date).stream()
                .map(record -> toViewDTO(normalizedAreaId, record))
                .collect(Collectors.toList());
    }

    private WeatherDataViewDTO toViewDTO(String areaId, WeatherData record) {
        return new WeatherDataViewDTO(
                areaId,
                record.getDate(),
                WeatherTimeFormatter.normalize(record.getStartTime()),
                WeatherTimeFormatter.normalize(record.getEndTime()),
                record.getWind().getSpeedKnots(),
                record.getWind().getDirectionDegrees(),
                record.getTemperature().getCelsius(),
                record.getPressure().getHPa(),
                record.getHumidity().getPercentage(),
                record.getAltitude().getMeters()
        );
    }
}
