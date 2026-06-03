package pt.isep.sem4pi.domain.model.weatherdata;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.Assert.*;

public class WeatherImportBatchTest {

    private WeatherData createValidWeatherData() {
        LocalTime now = LocalTime.now();
        return new WeatherData(
                LocalDate.now(), now, now,
                new Wind(10.0, 90.0), new Temperature(20.0),
                new AtmosphericPressure(1013.0), new Humidity(50.0), new Altitude(100.0)
        );
    }

    @Test
    public void ensureValidManualBatchIsCreated() {
        WeatherData data = createValidWeatherData();

        WeatherImportBatch batch = new WeatherImportBatch("AREA_PT", data);

        assertEquals("AREA_PT", batch.getAirControlAreaId());
        assertEquals(ImportBatchStatus.COMPLETED, batch.getStatus());
        assertEquals(1, batch.getImportedRecords());
        assertEquals(1, batch.getRecords().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfAreaIdIsNull() {
        new WeatherImportBatch(null, createValidWeatherData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFailsIfWeatherDataIsNull() {
        new WeatherImportBatch("AREA_PT", null);
    }

    @Test
    public void ensureValidBulkBatchIsCreated() {
        WeatherData data = createValidWeatherData();

        WeatherImportBatch batch = WeatherImportBatch.forBulkImport(
                "AREA_PT",
                java.util.List.of(data)
        );

        assertEquals("AREA_PT", batch.getAirControlAreaId());
        assertEquals(ImportBatchStatus.COMPLETED, batch.getStatus());
        assertEquals(1, batch.getImportedRecords());
        assertEquals(1, batch.getRecords().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBulkBatchFailsIfRecordsAreEmpty() {
        WeatherImportBatch.forBulkImport("AREA_PT", java.util.List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBulkBatchFailsIfStatusIsNull() {
        new WeatherImportBatch("AREA_PT", java.util.List.of(createValidWeatherData()), null);
    }
}