package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCode;
import pt.isep.sem4pi.domain.model.airport.IcaoCode;

import java.util.Optional;

public interface AirportRepository {
    Airport save(Airport airport);
    Optional<Airport> findByIataCode(IataCode iataCode);
    Optional<Airport> findByIcaoCode(IcaoCode icaoCode);
}