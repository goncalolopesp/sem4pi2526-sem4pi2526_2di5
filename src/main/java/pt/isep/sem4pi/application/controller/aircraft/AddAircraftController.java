package pt.isep.sem4pi.application.controller.aircraft;

import pt.isep.sem4pi.application.service.aircraft.AddAircraftService;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

import java.util.List;

public class AddAircraftController {
    private final AddAircraftService service;

    public AddAircraftController(AirTransportCompanyRepository cRepo, AircraftModelRepository mRepo) {
        this.service = new AddAircraftService(cRepo, mRepo);
    }

    public void addAircraft(String companyIata, String reg, String manufacturer, String model, String country, int e, int b, int f) {
        service.addAircraftToFleet(companyIata, reg, manufacturer, model, country, e, b, f);
    }

    public List<AirTransportCompany> getAirTransportCompanyList()
    {
        return service.getAirTransportCompanyList();
    }
}