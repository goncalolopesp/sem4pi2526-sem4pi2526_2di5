package pt.isep.sem4pi.application.controller.weatherdata;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.weatherdata.*;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryWeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testa a fronteira do controller da US043.
 *
 * <p>O controller deve garantir os papeis autorizados a consultar dados
 * meteorologicos e devolver a camada de apresentacao os DTOs do servico sem
 * alteracoes.</p>
 */
public class ConsultWeatherDataControllerTest {

    /**
     * Prova que o controller autoriza os papeis da US043 e devolve as medicoes
     * filtradas para a area/data pedida.
     */
    @Test
    public void ensureControllerReturnsFilteredResults() {
        InMemoryWeatherBatchRepository repository = new InMemoryWeatherBatchRepository();
        LocalDate date = LocalDate.of(2026, 6, 22);
        WeatherData data = new WeatherData(
                date,
                LocalTime.of(5, 0),
                LocalTime.of(8, 15),
                new Wind(12.0, 90.0),
                new Temperature(20.0),
                new AtmosphericPressure(1013.0),
                new Humidity(55.0),
                new Altitude(150.0)
        );
        repository.save(WeatherImportBatch.forBulkImport("121", List.of(data)));

        AuthorizationService authz = mock(AuthorizationService.class);
        ConsultWeatherDataController controller = new ConsultWeatherDataController(authz, repository);

        List<WeatherDataViewDTO> results = controller.consultByAreaAndDate("121", date);

        assertEquals(1, results.size());
        assertEquals(LocalTime.of(5, 0), results.get(0).getStartTime());
        assertEquals(LocalTime.of(8, 15), results.get(0).getEndTime());
        verify(authz).ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.PILOT,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.FLIGHT_CONTROL_OPERATOR
        );
    }
}

