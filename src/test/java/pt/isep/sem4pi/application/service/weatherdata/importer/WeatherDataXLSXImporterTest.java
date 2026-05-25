package pt.isep.sem4pi.application.service.weatherdata.importer;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeatherDataXLSXImporterTest {

    @Test
    public void ensureImporterParsesKnownXlsxFile() {
        String filePath = Path.of("src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData",
                "HP_WeatherData_v0.xlsx").toString();

        WeatherDataXLSXImporter importer = new WeatherDataXLSXImporter();
        assertTrue(importer.validate(filePath));

        WeatherImportPayload payload = importer.execute(filePath);
        assertFalse(payload.getRecords().isEmpty());
        assertTrue(payload.getTotalRecords() > 0);
    }
}
