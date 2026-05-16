package pt.isep.sem4pi.application.service.airtransportcompany;

import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.shared.IataCode;
import pt.isep.sem4pi.domain.model.shared.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

public class RegisterAirTransportCompanyService {

    private final AirTransportCompanyRepository repository;

    public RegisterAirTransportCompanyService(AirTransportCompanyRepository repository) {
        this.repository = repository;
    }

    public AirTransportCompany registerCompany(String rawName, String rawIata, String rawIcao) {
        CompanyName name = new CompanyName(rawName);
        IataCode iataCode = new IataCode(rawIata);
        IcaoCode icaoCode = new IcaoCode(rawIcao);

        if (repository.findByIataCode(iataCode).isPresent()) {
            throw new IllegalStateException("A company with this IATA code already exists.");
        }
        if (repository.findByIcaoCode(icaoCode).isPresent()) {
            throw new IllegalStateException("A company with this ICAO code already exists.");
        }

        AirTransportCompany newCompany = new AirTransportCompany(name, iataCode, icaoCode);

        return repository.save(newCompany);
    }
}