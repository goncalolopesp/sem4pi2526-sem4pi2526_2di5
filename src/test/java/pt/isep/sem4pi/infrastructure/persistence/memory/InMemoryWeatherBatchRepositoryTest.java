package pt.isep.sem4pi.infrastructure.persistence.memory;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.weatherdata.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class InMemoryWeatherBatchRepositoryTest {

    @Test
    public void ensureTotalWeatherRecordsSumsImportedRecords() {
        InMemoryWeatherBatchRepository repository = new InMemoryWeatherBatchRepository();

        WeatherData data1 = new WeatherData(
                LocalDate.now(), LocalTime.now(), LocalTime.now(),
                new Wind(10.0, 90.0), new Temperature(20.0),
                new AtmosphericPressure(1013.0), new Humidity(50.0), new Altitude(100.0)
        );
        WeatherData data2 = new WeatherData(
                LocalDate.now(), LocalTime.now(), LocalTime.now(),
                new Wind(12.0, 100.0), new Temperature(21.0),
                new AtmosphericPressure(1012.0), new Humidity(55.0), new Altitude(120.0)
        );

        repository.save(WeatherImportBatch.forBulkImport("ACA1", java.util.List.of(data1, data2)));
        repository.save(new WeatherImportBatch("ACA2", data1));

        assertEquals(3, repository.totalWeatherRecords());
    }
}

