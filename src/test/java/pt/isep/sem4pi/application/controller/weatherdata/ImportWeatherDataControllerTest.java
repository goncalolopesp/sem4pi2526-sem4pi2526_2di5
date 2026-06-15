package pt.isep.sem4pi.application.controller.weatherdata;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.application.service.weatherdata.WeatherDataImportService;
import pt.isep.sem4pi.application.service.weatherdata.importer.WeatherDataImporterFactory;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirControlAreaRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryWeatherBatchRepository;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the US042 controller boundary.
 *
 * <p>The purpose is to prove that authorization is checked before the bulk
 * import use case is executed, while still verifying that the controller
 * delegates to the real import service.</p>
 */
public class ImportWeatherDataControllerTest {

    /**
     * Happy path for importing one CSV row through the controller.
     *
     * <p>This is the defence reminder that US042 is role-protected at
     * controller level and that valid rows are persisted in a weather batch.</p>
     */
    @Test
    public void ensureImportWeatherDataDelegatesAndAuthorizes() throws Exception {
        InMemoryWeatherBatchRepository batchRepository = new InMemoryWeatherBatchRepository();
        InMemoryAirControlAreaRepository airControlAreaRepository = new InMemoryAirControlAreaRepository();
        airControlAreaRepository.save(new AirControlArea(
                "121",
                new AirControlAreaBoundary(37.0, 43.9, -9.8, 4.94),
                "Test ACA",
                0.0
        ));

        Path tempFile = Files.createTempFile("weather", ".csv");
        Files.writeString(tempFile, String.join(System.lineSeparator(),
                "ACA,Lat.,Long.,Lat.,Long.,Alt_inf (ft),Alt_Sup (ft),Direction (degree),Value (knot),Day,Star,End",
                "121,43.840454,-9.795711,40.225,-7.9501,0,1000,90,28.75,6/22/2026,5:00,8:15"
        ));

        WeatherDataImportService service = new WeatherDataImportService(
                batchRepository,
                airControlAreaRepository,
                new WeatherDataImporterFactory()
        );
        AuthorizationService authz = mock(AuthorizationService.class);
        ImportWeatherDataController controller = new ImportWeatherDataController(service, authz);

        WeatherImportResultDTO result = controller.importWeatherData(tempFile.toString(), "csv");

        assertEquals(1, result.getImportedRecords());
        assertEquals(0, result.getRejectedRecords());
        assertTrue(!batchRepository.findAllByAreaId("121").isEmpty());
        verify(authz).ensureAuthenticatedUserHasAnyOf(
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN,
                pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.WEATHER_PERSON
        );
    }
}
