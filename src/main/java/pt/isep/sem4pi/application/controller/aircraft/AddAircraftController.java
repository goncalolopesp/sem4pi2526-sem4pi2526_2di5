package pt.isep.sem4pi.application.controller.aircraft;

import pt.isep.sem4pi.application.service.aircraft.AddAircraftService;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelTempRepository;

import java.util.List;

public class AddAircraftController {

    private final AddAircraftService service;

    public AddAircraftController(AirTransportCompanyRepository cRepo,
                                 AircraftModelTempRepository mRepo) {
        this.service = new AddAircraftService(cRepo, mRepo);
    }

    public void addAircraft(String companyIata,
                            String reg,
                            String manufacturerCode,
                            String modelName,
                            String country,
                            int e, int b, int f) {
        service.addAircraftToFleet(companyIata, reg, manufacturerCode, modelName, country, e, b, f);
    }

    public List<AirTransportCompany> getAirTransportCompanyList() {
        return service.getAirTransportCompanyList();
    }
}