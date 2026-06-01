package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.*;
import pt.isep.sem4pi.domain.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.util.List;

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
    public long totalWeatherRecords() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(b.importedRecords), 0) FROM WeatherImportBatch b", Long.class);
        Long total = query.getSingleResult();
        em.close();
        return total == null ? 0L : total;
    }
}