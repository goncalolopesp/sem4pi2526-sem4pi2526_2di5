package pt.isep.sem4pi.application.service.weatherdata.importer;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Parser tests for the US042 CSV weather importer.
 *
 * <p>They document the expected source layout: wind files provide ACA,
 * altitude limits, direction, wind speed, day and start/end times.</p>
 */
public class WeatherDataCSVImporterTest {

    /**
     * Confirms that the real sample CSV file is recognized and converted into
     * parsed weather DTOs.
     */
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

    /**
     * Invalid numeric values are reported as row errors instead of throwing and
     * aborting the whole file.
     */
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

    /**
     * Confirms that the importer keeps both start and end times, which are
     * later shown in US043 and selected in US082.
     */
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

    /**
     * A row with no usable start or end time is rejected because the system
     * cannot know the validity interval of the weather data.
     */
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
