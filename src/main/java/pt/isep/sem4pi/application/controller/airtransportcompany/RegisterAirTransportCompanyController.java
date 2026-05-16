package pt.isep.sem4pi.application.controller.airtransportcompany;

import pt.isep.sem4pi.application.service.airtransportcompany.RegisterAirTransportCompanyService;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

public class RegisterAirTransportCompanyController {

    private final RegisterAirTransportCompanyService service;

    public RegisterAirTransportCompanyController(AirTransportCompanyRepository repository) {
        this.service = new RegisterAirTransportCompanyService(repository);
    }

    public AirTransportCompany registerAirTransportCompany(String name, String iata, String icao) {
        return service.registerCompany(name, iata, icao);
    }
}