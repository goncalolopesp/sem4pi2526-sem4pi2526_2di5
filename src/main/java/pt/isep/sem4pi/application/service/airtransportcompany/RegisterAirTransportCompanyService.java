package pt.isep.sem4pi.application.service.airtransportcompany;

import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

public class RegisterAirTransportCompanyService {

    private final AirTransportCompanyRepository repository;

    public RegisterAirTransportCompanyService(AirTransportCompanyRepository repository) {
        this.repository = repository;
    }

    public AirTransportCompany registerCompany(String rawName, String rawIata, String rawIcao) {
        CompanyName name = new CompanyName(rawName);
        IataCodeCompany iataCode = new IataCodeCompany(rawIata);
        IcaoCodeCompany icaoCode = new IcaoCodeCompany(rawIcao);

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