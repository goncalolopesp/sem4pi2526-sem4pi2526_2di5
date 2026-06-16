package pt.isep.sem4pi.application.service.weatherdata;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.weatherdata.*;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryWeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testes de servico para a consulta meteorologica da US043.
 *
 * <p>Provam que a consulta e filtrada pelos dois criterios obrigatorios:
 * Air Control Area e data de calendario.</p>
 */
public class ConsultWeatherDataServiceTest {

    /**
     * Apenas dados que correspondem a area e data pedidas devem ser devolvidos;
     * registos de outra area ou outro dia sao excluidos.
     */
    @Test
    public void ensureFindByAreaAndDateFiltersCorrectly() {
        InMemoryWeatherBatchRepository repository = new InMemoryWeatherBatchRepository();
        ConsultWeatherDataService service = new ConsultWeatherDataService(repository);

        LocalDate targetDate = LocalDate.of(2026, 5, 27);
        WeatherData matching = new WeatherData(
                targetDate,
                LocalTime.of(5, 0),
                LocalTime.of(8, 15),
                new Wind(12.0, 90.0),
                new Temperature(20.0),
                new AtmosphericPressure(1013.0),
                new Humidity(55.0),
                new Altitude(150.0)
        );
        WeatherData otherDate = new WeatherData(
                targetDate.minusDays(1),
                LocalTime.of(6, 0),
                LocalTime.of(7, 30),
                new Wind(8.0, 180.0),
                new Temperature(18.0),
                new AtmosphericPressure(1011.0),
                new Humidity(60.0),
                new Altitude(200.0)
        );

        repository.save(WeatherImportBatch.forBulkImport("ACA1", List.of(matching, otherDate)));
        repository.save(WeatherImportBatch.forBulkImport("ACA2", List.of(matching)));

        List<WeatherDataViewDTO> results = service.findByAreaAndDate("ACA1", targetDate);

        assertEquals(1, results.size());
        WeatherDataViewDTO dto = results.get(0);
        assertEquals("ACA1", dto.getAirControlAreaId());
        assertEquals(targetDate, dto.getDate());
        assertEquals(LocalTime.of(5, 0), dto.getStartTime());
        assertEquals(LocalTime.of(8, 15), dto.getEndTime());
    }

    /**
     * Identificadores de area em branco sao rejeitados para evitar queries sem sentido.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsWithBlankArea() {
        ConsultWeatherDataService service = new ConsultWeatherDataService(new InMemoryWeatherBatchRepository());
        service.findByAreaAndDate(" ", LocalDate.now());
    }

    /**
     * A data e obrigatoria porque a US043 e explicitamente uma consulta por dia.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsWithNullDate() {
        ConsultWeatherDataService service = new ConsultWeatherDataService(new InMemoryWeatherBatchRepository());
        service.findByAreaAndDate("121", null);
    }
}
