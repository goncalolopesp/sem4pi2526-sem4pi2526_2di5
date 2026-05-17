package pt.isep.sem4pi.application.service.aircraft;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftModelTemp;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelTempRepository;

public class AddAircraftService {

    private final AirTransportCompanyRepository companyRepo;
    private final AircraftModelTempRepository modelRepo;

    public AddAircraftService(AirTransportCompanyRepository companyRepo, AircraftModelTempRepository modelRepo) {
        this.companyRepo = companyRepo;
        this.modelRepo = modelRepo;
    }

    public void addAircraftToFleet(String companyIataStr, String regStr, String modelId,
                                   String country, int eco, int bus, int first) {

        IataCode companyIata = new IataCode(companyIataStr);
        AircraftRegistration registration = new AircraftRegistration(regStr);
        CabinConfiguration seats = new CabinConfiguration(eco, bus, first);

        AirTransportCompany company = companyRepo.findByIataCode(companyIata)
                .orElseThrow(() -> new IllegalArgumentException("Company not found."));

        AircraftModelTemp model = modelRepo.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft Model not found in system."));

        if (seats.getTotalCapacity() > model.getMaximumCapacity()) {
            throw new IllegalStateException("Total seats exceed the maximum capacity for model " + modelId);
        }

        if (companyRepo.existsByAircraftRegistration(registration)) {
            throw new IllegalStateException("An aircraft with this registration already exists.");
        }

        Aircraft newAircraft = new Aircraft(registration, modelId, country, seats);
        company.addAircraftToFleet(newAircraft);

        companyRepo.save(company);
    }
}