package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryAirTransportCompanyRepository implements AirTransportCompanyRepository {

    private final List<AirTransportCompany> database = new ArrayList<>();

    @Override
    public AirTransportCompany save(AirTransportCompany company) {
        database.add(company);
        return company;
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(IataCode iataCode) {
        return database.stream()
                .filter(c -> c.identityIata().equals(iataCode))
                .findFirst();
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode) {
        return database.stream()
                .filter(c -> c.identityIcao().equals(icaoCode))
                .findFirst();
    }

    @Override
    public Iterable<AirTransportCompany> findAll() {
        return database;
    }

    @Override
    public boolean existsByAircraftRegistration(AircraftRegistration registration) {
        return database.stream()
                .flatMap(c -> c.getFleet().stream())
                .anyMatch(a -> a.getRegistration().equals(registration));
    }
}