package pt.isep.sem4pi.infrastructure.persistence.JPA;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCode;
import pt.isep.sem4pi.domain.model.airport.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.util.Optional;

public class JpaAirportRepository implements AirportRepository {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("sem4piPU");

    @Override
    public Airport save(Airport airport) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Airport savedAirport = em.merge(airport);
        em.getTransaction().commit();
        em.close();
        return savedAirport;
    }

    @Override
    public Optional<Airport> findByIataCode(IataCode iataCode) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Airport> query = em.createQuery(
                "SELECT a FROM Airport a WHERE a.iataCode = :iata", Airport.class);
        query.setParameter("iata", iataCode);

        Optional<Airport> result = query.getResultStream().findFirst();
        em.close();
        return result;
    }

    @Override
    public Optional<Airport> findByIcaoCode(IcaoCode icaoCode) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Airport> query = em.createQuery(
                "SELECT a FROM Airport a WHERE a.icaoCode = :icao", Airport.class);
        query.setParameter("icao", icaoCode);

        Optional<Airport> result = query.getResultStream().findFirst();
        em.close();
        return result;
    }
}