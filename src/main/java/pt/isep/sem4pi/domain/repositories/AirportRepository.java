package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airport.IcaoCodeAirport;

import java.util.Optional;

public interface AirportRepository extends DomainRepository<IataCodeAirport, Airport> {
    Optional<Airport> findByIcaoCode(IcaoCodeAirport icaoCode);
}