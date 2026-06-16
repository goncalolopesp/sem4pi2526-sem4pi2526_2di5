package pt.isep.sem4pi.application.service.weatherdata;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirControlAreaRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryWeatherBatchRepository;

import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Testes ao nivel do servico para a importacao meteorologica em massa da US042.
 *
 * <p>Estes testes focam as regras de orquestracao: o servico recebe dados
 * meteorologicos ja analisados, garante que a Air Control Area existe, cria
 * lotes por area e rejeita formatos nao suportados.</p>
 */
public class WeatherDataImportServiceTest {

    private InMemoryWeatherBatchRepository batchRepository;
    private WeatherDataImportService service;

    @Before
    public void setUp() {
        batchRepository = new InMemoryWeatherBatchRepository();
        InMemoryAirControlAreaRepository airControlAreaRepository = new InMemoryAirControlAreaRepository();
        airControlAreaRepository.save(new AirControlArea(
                "121",
                new AirControlAreaBoundary(37.0, 43.9, -9.8, 4.94),
                "Test ACA",
                0.0
        ));
        service = new WeatherDataImportService(
                batchRepository,
                airControlAreaRepository,
                new WeatherDataImporterFactory()
        );
    }

    /**
     * Prova que um CSV de vento valido cria um {@code WeatherImportBatch}
     * persistido para uma Air Control Area ja conhecida.
     */
    @Test
    public void ensureBulkImportCreatesBatchForValidArea() {
        String filePath = Path.of("src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData",
                "HP_WeatherData_v0-Wind.csv").toString();

        WeatherImportResultDTO result = service.importWeatherData(filePath, "csv");

        assertTrue(result.getImportedRecords() > 0);
        assertEquals(0, result.getRejectedRecords());
        assertEquals(1, batchRepository.findAllByAreaId("121").size());
    }

    /**
     * Prova o fallback da US042 em que uma ACA em falta e criada a partir do
     * ficheiro ACA auxiliar antes de guardar os respetivos registos
     * meteorologicos.
     */
    @Test
    public void ensureBulkImportCreatesMissingAcaFromCsv() {
        InMemoryWeatherBatchRepository localBatchRepository = new InMemoryWeatherBatchRepository();
        InMemoryAirControlAreaRepository localAirControlAreaRepository = new InMemoryAirControlAreaRepository();
        WeatherDataImportService localService = new WeatherDataImportService(
                localBatchRepository,
                localAirControlAreaRepository,
                new WeatherDataImporterFactory()
        );

        String windPath = java.nio.file.Path.of(
                "src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData",
                "HP_WeatherData_v0-Wind.csv").toString();

        WeatherImportResultDTO result = localService.importWeatherData(windPath, "csv");

        assertTrue(result.getImportedRecords() > 0);
        assertTrue(localAirControlAreaRepository.containsOfIdentity(
                new AirControlAreaID("121")));
    }

    /**
     * Formatos nao suportados devem falhar cedo porque nao existe estrategia de importacao.
     */
    @Test(expected = IllegalArgumentException.class)
    public void ensureInvalidFileTypeThrows() {
        service.importWeatherData("missing_file.txt", "txt");
    }
}
