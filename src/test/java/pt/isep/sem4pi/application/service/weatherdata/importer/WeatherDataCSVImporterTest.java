package pt.isep.sem4pi.application.service.weatherdata.importer;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class WeatherDataCSVImporterTest {

    @Test
    public void ensureImporterParsesKnownWindFile() {
        String filePath = Path.of("src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData",
                "HP_WeatherData_v0-Wind.csv").toString();

        WeatherDataCSVImporter importer = new WeatherDataCSVImporter();
        assertTrue(importer.validate(filePath));

        WeatherImportPayload payload = importer.execute(filePath);
        assertTrue(payload.getRecords().size() > 0);
        assertTrue(payload.getTotalRecords() > 0);
    }

    @Test
    public void ensureImporterReportsInvalidLine() throws IOException {
        Path tempFile = Files.createTempFile("weather", ".csv");
        Files.writeString(tempFile, "ACA,Lat.,Long.,Lat.,Long.,Alt_inf (ft),Alt_Sup (ft),Direction (degree),Value (knot),Day,Star,End\n"
                + "121,43.0,-9.0,40.0,-7.9,invalid,1000,90,25,6/22/2026,5:00,7:30\n");

        WeatherDataCSVImporter importer = new WeatherDataCSVImporter();
        WeatherImportPayload payload = importer.execute(tempFile.toString());

        assertEquals(0, payload.getRecords().size());
        assertEquals(1, payload.getErrors().size());
    }

    @Test
    public void ensureImporterParsesStartAndEndTimes() throws IOException {
        Path tempFile = Files.createTempFile("weather", ".csv");
        Files.writeString(tempFile, "ACA,Lat.,Long.,Lat.,Long.,Alt_inf (ft),Alt_Sup (ft),Direction (degree),Value (knot),Day,Star,End\n"
                + "121,43.0,-9.0,40.0,-7.9,0,1000,90,25,6/22/2026,5:00,8:15\n");

        WeatherDataCSVImporter importer = new WeatherDataCSVImporter();
        WeatherImportPayload payload = importer.execute(tempFile.toString());

        assertEquals(1, payload.getRecords().size());
        assertEquals(java.time.LocalTime.of(5, 0), payload.getRecords().get(0).getStartTime());
        assertEquals(java.time.LocalTime.of(8, 15), payload.getRecords().get(0).getEndTime());
    }

    @Test
    public void ensureImporterReportsInvalidTimeRange() throws IOException {
        Path tempFile = Files.createTempFile("weather", ".csv");
        Files.writeString(tempFile, "ACA,Lat.,Long.,Lat.,Long.,Alt_inf (ft),Alt_Sup (ft),Direction (degree),Value (knot),Day,Star,End\n"
                + "121,43.0,-9.0,40.0,-7.9,0,1000,90,25,6/22/2026,invalid,\n");

        WeatherDataCSVImporter importer = new WeatherDataCSVImporter();
        WeatherImportPayload payload = importer.execute(tempFile.toString());

        assertEquals(0, payload.getRecords().size());
        assertEquals(1, payload.getErrors().size());
    }
}
