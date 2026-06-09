package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
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
    public long countWeatherDataByAreaIdAndDate(final String areaId, final LocalDate date) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(wd) FROM WeatherImportBatch b JOIN b.records wd "
                        + "WHERE b.airControlAreaId = :areaId AND wd.date = :date",
                Long.class);
        query.setParameter("areaId", areaId);
        query.setParameter("date", date);
        Long count = query.getSingleResult();
        em.close();
        return count == null ? 0L : count;
    }

    @Override
    public List<WeatherData> findWeatherDataByAreaIdAndDate(final String areaId, final LocalDate date) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<WeatherData> query = em.createQuery(
                "SELECT wd FROM WeatherImportBatch b JOIN b.records wd "
                        + "WHERE b.airControlAreaId = :areaId AND wd.date = :date "
                        + "ORDER BY wd.startTime, wd.endTime",
                WeatherData.class);
        query.setParameter("areaId", areaId);
        query.setParameter("date", date);
        List<WeatherData> results = query.getResultList();
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
    public long totalWeatherRecords() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(b.importedRecords), 0) FROM WeatherImportBatch b", Long.class);
        Long total = query.getSingleResult();
        em.close();
        return total == null ? 0L : total;
    }
}