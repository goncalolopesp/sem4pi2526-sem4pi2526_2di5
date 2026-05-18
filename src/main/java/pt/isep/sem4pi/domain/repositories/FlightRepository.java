package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;

/**
 * Repositorio de voos com suporte a validacao de voos pendentes (US71).
 */
public interface FlightRepository extends DomainRepository<FlightId, Flight> {
    Iterable<Flight> findByAircraft(AircraftRegistration registration);

    /**
     * Indica se existem voos pendentes associados a uma aeronave.
     */
    boolean hasPendingFlights(AircraftRegistration registration);
}
