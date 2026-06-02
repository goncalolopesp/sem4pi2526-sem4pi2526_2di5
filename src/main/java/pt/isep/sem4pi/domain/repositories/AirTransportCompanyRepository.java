package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;

import java.util.Optional;

public interface AirTransportCompanyRepository {
    AirTransportCompany save(AirTransportCompany company);
    Optional<AirTransportCompany> findByIataCode(IataCodeCompany iataCode);
    Optional<AirTransportCompany> findByIcaoCode(IcaoCodeCompany icaoCode);
    Iterable<AirTransportCompany> findAll();
    boolean existsByAircraftRegistration(AircraftRegistration registration);
}
