package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.flight.Flight;
import pt.isep.sem4pi.domain.flight.FlightId;

public interface FlightRepository extends DomainRepository<FlightId, Flight> {
    Iterable<Flight> findByAircraft(AircraftRegistration registration);

    boolean hasPendingFlights(AircraftRegistration registration);
}

