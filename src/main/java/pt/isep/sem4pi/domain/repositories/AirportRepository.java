package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.airport.Airport;
import pt.isep.sem4pi.domain.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.airport.IcaoCodeAirport;
import pt.isep.sem4pi.domain.route.FlightRoute;
import pt.isep.sem4pi.domain.route.FlightRouteID;

import java.util.Optional;

public interface AirportRepository extends DomainRepository<IataCodeAirport, Airport> {
    Optional<Airport> findByIcaoCode(IcaoCodeAirport icaoCode);
}