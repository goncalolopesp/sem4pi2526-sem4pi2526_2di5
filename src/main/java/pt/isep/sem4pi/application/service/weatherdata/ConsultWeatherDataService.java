package pt.isep.sem4pi.application.service.weatherdata;

import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherTimeFormatter;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementa a consulta de dados meteorologicos da US043.
 *
 * <p>A filtragem e delegada ao {@link WeatherBatchRepository}, permitindo que
 * a implementacao JPA execute a query na base de dados em vez de carregar
 * todos os lotes de importacao em memoria. As entidades de dominio sao
 * mapeadas para DTOs de visualizacao antes de chegarem a camada de
 * apresentacao.</p>
 */
public class ConsultWeatherDataService {

    private final WeatherBatchRepository repository;

    public ConsultWeatherDataService(WeatherBatchRepository repository) {
        this.repository = repository;
    }

    /**
     * Encontra todas as medicoes de uma area na data pedida.
     *
     * <p>A contagem preliminar evita obter e mapear registos quando nao existe
     * correspondencia. As horas sao normalizadas antes da apresentacao para
     * remover segundos/nanossegundos irrelevantes introduzidos por formatos
     * externos.</p>
     *
     * @param areaId identificador da Air Control Area
     * @param date dia pedido
     * @return medicoes encontradas representadas como DTOs de apresentacao
     * @throws IllegalArgumentException quando a area ou a data esta em falta
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
