package pt.isep.sem4pi.application.service.weatherdata;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.weatherdata.ImportBatchStatus;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryWeatherBatchRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class RegisterWeatherServiceTest {

    private InMemoryWeatherBatchRepository inMemoryRepo;
    private RegisterWeatherService service;

    @Before
    public void setUp() {
        inMemoryRepo = new InMemoryWeatherBatchRepository();
        service = new RegisterWeatherService(inMemoryRepo);
    }

    @Test
    public void ensureSuccessfulManualWeatherRegistration() {
        WeatherImportBatch result = service.registerManualData(
                "EUR_WEST", LocalDate.of(2026, 6, 10), 22.0, 10.0, 90.0, 60.0, 1012.0, 150.0);

        assertNotNull(result);
        assertEquals("EUR_WEST", result.getAirControlAreaId());
        assertEquals(ImportBatchStatus.COMPLETED, result.getStatus());
        assertEquals(1, result.getImportedRecords());

        List<WeatherImportBatch> savedBatches = inMemoryRepo.findAllByAreaId("EUR_WEST");
        assertEquals(1, savedBatches.size());
    }

    @Test
    public void ensureRegistrationFailsAndNothingIsSavedWithInvalidData() {
        // Act & Assert
        try {
            service.registerManualData(
                    "EUR_WEST", LocalDate.of(2026, 6, 10), 22.0, 10.0, 90.0, 60.0, 1012.0, 60000.0);
            fail("Expected exception due to invalid altitude");
        } catch (IllegalArgumentException e) {
            assertEquals("Altitude must be a valid atmospheric height.", e.getMessage());
        }

        List<WeatherImportBatch> savedBatches = inMemoryRepo.findAllByAreaId("EUR_WEST");
        assertEquals(0, savedBatches.size());
    }
}