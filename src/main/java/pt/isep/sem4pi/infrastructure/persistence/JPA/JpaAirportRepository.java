package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airport.IcaoCodeAirport;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.util.Optional;

public class JpaAirportRepository
        extends JpaAutoTxRepository<Airport, IataCodeAirport, IataCodeAirport>
        implements AirportRepository {

    public JpaAirportRepository() {
        super("sem4piPU", "iataCode");
    }

    @Override
    public Optional<Airport> findByIcaoCode(IcaoCodeAirport icaoCode) {
        final TypedQuery<Airport> query = entityManager().createQuery(
                "SELECT a FROM Airport a WHERE a.icaoCode = :icao", Airport.class);
        query.setParameter("icao", icaoCode);

        return query.getResultStream().findFirst();
    }
}