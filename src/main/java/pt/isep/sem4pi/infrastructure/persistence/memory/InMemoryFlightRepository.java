package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.flight.Flight;
import pt.isep.sem4pi.domain.flight.FlightId;
import pt.isep.sem4pi.domain.repositories.FlightRepository;

public class InMemoryFlightRepository extends InMemoryDomainRepository<Flight, FlightId> implements FlightRepository {

    @Override
    public Iterable<Flight> findByAircraft(final AircraftRegistration registration) {
        return match(flight -> flight.aircraftRegistration().equals(registration));
    }

    @Override
    public boolean hasPendingFlights(final AircraftRegistration registration) {
        return match(flight -> flight.aircraftRegistration().equals(registration) && flight.isPending())
                .iterator()
                .hasNext();
    }
}

