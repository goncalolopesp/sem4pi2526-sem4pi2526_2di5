package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
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
    public Optional<AirTransportCompany> findByIataCode(IataCode iataCode) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<AirTransportCompany> query = em.createQuery(
                "SELECT c FROM AirTransportCompany c WHERE c.iataCode = :iata", AirTransportCompany.class);
        query.setParameter("iata", iataCode);

        Optional<AirTransportCompany> result = query.getResultStream().findFirst();
        em.close();
        return result;
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode) {
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
}
