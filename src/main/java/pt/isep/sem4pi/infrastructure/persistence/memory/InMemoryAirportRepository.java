package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCode;
import pt.isep.sem4pi.domain.model.airport.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryAirportRepository implements AirportRepository {

    private final List<Airport> database = new ArrayList<>();

    @Override
    public Airport save(Airport airport) {
        database.add(airport);
        return airport;
    }

    @Override
    public Optional<Airport> findByIataCode(IataCode iataCode) {
        return database.stream()
                .filter(a -> a.identityIata().equals(iataCode))
                .findFirst();
    }

    @Override
    public Optional<Airport> findByIcaoCode(IcaoCode icaoCode) {
        return database.stream()
                .filter(a -> a.identityIcao().equals(icaoCode))
                .findFirst();
    }
}