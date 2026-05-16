package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.shared.IataCode;
import pt.isep.sem4pi.domain.model.shared.IcaoCode;

import java.util.Optional;

public interface AirTransportCompanyRepository {
    AirTransportCompany save(AirTransportCompany company);
    Optional<AirTransportCompany> findByIataCode(IataCode iataCode);
    Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode);
    Iterable<AirTransportCompany> findAll();
}
