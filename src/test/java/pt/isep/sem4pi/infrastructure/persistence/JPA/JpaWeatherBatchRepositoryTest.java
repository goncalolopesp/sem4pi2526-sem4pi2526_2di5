package pt.isep.sem4pi.infrastructure.persistence.JPA;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.weatherdata.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JpaWeatherBatchRepositoryTest {

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("sem4piPU");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM flight_plan_weather_data").executeUpdate();
        em.createQuery("DELETE FROM WeatherData").executeUpdate();
        em.createQuery("DELETE FROM WeatherImportBatch").executeUpdate();
        em.getTransaction().commit();
        em.close();
        emf.close();
    }

    @Test
    public void ensureFindAllByAreaIdFetchesRecords() {
        JpaWeatherBatchRepository repository = new JpaWeatherBatchRepository();
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

        WeatherImportBatch batch = WeatherImportBatch.forBulkImport("121", List.of(data));
        repository.save(batch);

        List<WeatherImportBatch> results = repository.findAllByAreaId("121");

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getRecords().size());
        assertEquals(LocalTime.of(5, 0), results.get(0).getRecords().get(0).getStartTime());
        assertEquals(LocalTime.of(8, 15), results.get(0).getRecords().get(0).getEndTime());
    }

    @Test
    public void ensureTwoConsecutiveSavesForSameAreaWork() {
        JpaWeatherBatchRepository repository = new JpaWeatherBatchRepository();
        LocalDate date = LocalDate.of(2026, 6, 11);
        WeatherData first = new WeatherData(
                date,
                LocalTime.of(8, 0),
                LocalTime.of(8, 45),
                new Wind(10.0, 90.0),
                new Temperature(15.0),
                new AtmosphericPressure(1013.0),
                new Humidity(50.0),
                new Altitude(1000.0)
        );
        WeatherData second = new WeatherData(
                date,
                LocalTime.of(8, 45),
                LocalTime.of(9, 30),
                new Wind(4.0, 50.0),
                new Temperature(29.0),
                new AtmosphericPressure(1013.0),
                new Humidity(70.0),
                new Altitude(3045.0)
        );

        repository.save(new WeatherImportBatch("121", first));
        repository.save(new WeatherImportBatch("121", second));

        assertEquals(2, repository.countWeatherDataByAreaIdAndDate("121", date));
    }
}


