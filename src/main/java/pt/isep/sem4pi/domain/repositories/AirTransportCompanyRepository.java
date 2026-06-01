package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.airtransportcompany.IcaoCodeCompany;

import java.util.Optional;

public interface AirTransportCompanyRepository {
    AirTransportCompany save(AirTransportCompany company);
    Optional<AirTransportCompany> findByIataCode(IataCodeCompany iataCode);
    Optional<AirTransportCompany> findByIcaoCode(IcaoCodeCompany icaoCode);
    Iterable<AirTransportCompany> findAll();
    boolean existsByAircraftRegistration(AircraftRegistration registration);
}
