package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.airport.Airport;
import pt.isep.sem4pi.domain.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.airport.IcaoCodeAirport;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

public class InMemoryAirportRepository extends InMemoryDomainRepository<Airport, IataCodeAirport> implements AirportRepository {

    @Override
    public Optional<Airport> findByIcaoCode(IcaoCodeAirport icaoCode) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.identityIcao().equals(icaoCode))
                .findFirst();
    }
}