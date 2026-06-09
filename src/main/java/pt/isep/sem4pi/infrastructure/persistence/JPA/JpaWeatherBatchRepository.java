package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class JpaWeatherBatchRepository implements WeatherBatchRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("sem4piPU");

    @Override
    public WeatherImportBatch save(WeatherImportBatch batch) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        WeatherImportBatch saved = em.merge(batch);
        em.getTransaction().commit();
        em.close();
        return saved;
    }

    @Override
    public List<WeatherImportBatch> findAll() {
        EntityManager em = emf.createEntityManager();
        List<WeatherImportBatch> results = em.createQuery(
                "SELECT DISTINCT b FROM WeatherImportBatch b LEFT JOIN FETCH b.records",
                WeatherImportBatch.class).getResultList();
        em.close();
        return results;
    }

    @Override
    public List<WeatherImportBatch> findAllByAreaId(String areaId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<WeatherImportBatch> query = em.createQuery(
                "SELECT DISTINCT b FROM WeatherImportBatch b LEFT JOIN FETCH b.records WHERE b.airControlAreaId = :areaId",
                WeatherImportBatch.class);
        query.setParameter("areaId", areaId);
        List<WeatherImportBatch> results = query.getResultList();
        em.close();
        return results;
    }

    @Override
    public Optional<WeatherData> findWeatherData(String areaId, LocalDate date,
                                                 LocalTime startTime, LocalTime endTime) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<WeatherData> query = em.createQuery(
                "SELECT w FROM WeatherImportBatch b JOIN b.records w "
                        + "WHERE b.airControlAreaId = :areaId AND w.date = :date "
                        + "AND w.startTime = :startTime AND w.endTime = :endTime",
                WeatherData.class);
        query.setParameter("areaId", areaId);
        query.setParameter("date", date);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        Optional<WeatherData> result = query.getResultStream().findFirst();
        em.close();
        return result;
    }

    @Override
    public long totalWeatherRecords() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(b.importedRecords), 0) FROM WeatherImportBatch b", Long.class);
        Long total = query.getSingleResult();
        em.close();
        return total == null ? 0L : total;
    }
}
