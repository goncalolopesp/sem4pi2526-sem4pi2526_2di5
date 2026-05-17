package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pt.isep.sem4pi.domain.model.aircraft.AircraftModelTemp;
import pt.isep.sem4pi.domain.repositories.AircraftModelTempRepository;

import java.util.Optional;

public class JpaAircraftModelTempRepository implements AircraftModelTempRepository {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("AlSafePU");

    @Override
    public AircraftModelTemp save(AircraftModelTemp model) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AircraftModelTemp savedModel = em.merge(model);
        em.getTransaction().commit();
        em.close();
        return savedModel;
    }

    @Override
    public Optional<AircraftModelTemp> findById(String modelId) {
        EntityManager em = emf.createEntityManager();
        AircraftModelTemp model = em.find(AircraftModelTemp.class, modelId);
        em.close();
        return Optional.ofNullable(model);
    }
}