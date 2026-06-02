package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

import java.util.Optional;


public class JpaAirTransportCompanyRepository implements AirTransportCompanyRepository {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("sem4piPU");

    @Override
    public AirTransportCompany save(AirTransportCompany company) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AirTransportCompany savedCompany = em.merge(company);

        em.getTransaction().commit();
        em.close();
        return savedCompany;
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(IataCodeCompany iataCode) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<AirTransportCompany> query = em.createQuery(
                    "SELECT c FROM AirTransportCompany c LEFT JOIN FETCH c.fleet WHERE c.iataCode = :iata",
                    AirTransportCompany.class
            );
            query.setParameter("iata", iataCode);

            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(IcaoCodeCompany icaoCode) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<AirTransportCompany> query = em.createQuery(
                "SELECT c FROM AirTransportCompany c WHERE c.icaoCode = :icao", AirTransportCompany.class);
        query.setParameter("icao", icaoCode);

        Optional<AirTransportCompany> result = query.getResultStream().findFirst();
        em.close();
        return result;
    }

    @Override
    public Iterable<AirTransportCompany> findAll() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<AirTransportCompany> query = em.createQuery("SELECT c FROM AirTransportCompany c", AirTransportCompany.class);
        Iterable<AirTransportCompany> results = query.getResultList();
        em.close();
        return results;
    }

    @Override
    public boolean existsByAircraftRegistration(AircraftRegistration registration) {
        EntityManager em = emf.createEntityManager();
        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM AirTransportCompany c JOIN c.fleet a WHERE a.id = :reg", Long.class)
                .setParameter("reg", registration)
                .getSingleResult();
        em.close();
        return count > 0;
    }
}
