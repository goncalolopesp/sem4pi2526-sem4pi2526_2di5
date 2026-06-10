package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherImportBatch;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherData;
import pt.isep.sem4pi.domain.repositories.WeatherBatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class JpaWeatherBatchRepository implements WeatherBatchRepository {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("sem4piPU");

    @Override
    public WeatherImportBatch save(final WeatherImportBatch batch) {
        final EntityManager em = EMF.createEntityManager();
        final var transaction = em.getTransaction();
        try {
            transaction.begin();
            final WeatherImportBatch saved = em.merge(batch);
            transaction.commit();
            return saved;
        } catch (final RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public long countWeatherDataByAreaIdAndDate(final String areaId, final LocalDate date) {
        EntityManager em = EMF.createEntityManager();
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
    public List<WeatherImportBatch> findAll() {
        EntityManager em = emf.createEntityManager();
        List<WeatherImportBatch> results = em.createQuery(
                "SELECT DISTINCT b FROM WeatherImportBatch b LEFT JOIN FETCH b.records",
                WeatherImportBatch.class).getResultList();
        em.close();
        return results;
    }

    @Override
    public List<WeatherData> findWeatherDataByAreaIdAndDate(final String areaId, final LocalDate date) {
        EntityManager em = EMF.createEntityManager();
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
        EntityManager em = EMF.createEntityManager();
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
        EntityManager em = EMF.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(b.importedRecords), 0) FROM WeatherImportBatch b", Long.class);
        Long total = query.getSingleResult();
        em.close();
        return total == null ? 0L : total;
    }
}
