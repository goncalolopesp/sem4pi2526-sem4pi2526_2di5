package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.flight.Flight;
import pt.isep.sem4pi.domain.flight.FlightId;
import pt.isep.sem4pi.domain.flight.FlightStatus;
import pt.isep.sem4pi.domain.repositories.FlightRepository;

public class JpaFlightRepository extends JpaAutoTxRepository<Flight, FlightId, FlightId> implements FlightRepository {

    public JpaFlightRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }

    @Override
    public Iterable<Flight> findByAircraft(final AircraftRegistration registration) {
        final TypedQuery<Flight> query = entityManager().createQuery(
                "SELECT f FROM Flight f WHERE f.aircraftRegistration = :registration", Flight.class);
        query.setParameter("registration", registration);
        return query.getResultList();
    }

    @Override
    public boolean hasPendingFlights(final AircraftRegistration registration) {
        final TypedQuery<Long> query = entityManager().createQuery(
                "SELECT COUNT(f) FROM Flight f WHERE f.aircraftRegistration = :registration AND f.status = :status",
                Long.class);
        query.setParameter("registration", registration);
        query.setParameter("status", FlightStatus.PENDING);
        return query.getSingleResult() > 0;
    }
}

