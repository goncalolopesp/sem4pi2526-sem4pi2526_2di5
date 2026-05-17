package pt.isep.sem4pi.application.service.aircraft;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftModelTemp;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelTempRepository;

import java.util.ArrayList;
import java.util.List;

public class AddAircraftService {

    private final AirTransportCompanyRepository companyRepo;
    private final AircraftModelTempRepository modelRepo;

    public AddAircraftService(AirTransportCompanyRepository companyRepo,
                              AircraftModelTempRepository modelRepo) {
        this.companyRepo = companyRepo;
        this.modelRepo = modelRepo;
    }

    public void addAircraftToFleet(String companyIataStr,
                                   String regStr,
                                   String manufacturerCode,
                                   String modelName,
                                   String country,
                                   int eco, int bus, int first) {

        final IataCode companyIata = new IataCode(companyIataStr);
        final AircraftRegistration registration = new AircraftRegistration(regStr);
        final CabinConfiguration seats = new CabinConfiguration(eco, bus, first);

        final AirTransportCompany company = companyRepo.findByIataCode(companyIata)
                .orElseThrow(() -> new IllegalArgumentException("Company not found."));

        // The temp model repository continues to identify models by a flat key (modelName)
        final AircraftModelTemp model = modelRepo.findById(modelName)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft Model not found in system."));

        if (seats.getTotalCapacity() > model.getMaximumCapacity()) {
            throw new IllegalStateException(
                    "Total seats exceed the maximum capacity for model " + modelName);
        }

        if (companyRepo.existsByAircraftRegistration(registration)) {
            throw new IllegalStateException("An aircraft with this registration already exists.");
        }

        final AircraftModelId id = new AircraftModelId(new ManufacturerId(manufacturerCode), modelName);
        final Aircraft newAircraft = new Aircraft(registration, id);
        company.addAircraftToFleet(newAircraft);

        companyRepo.save(company);
    }

    public List<AirTransportCompany> getAirTransportCompanyList() {
        final Iterable<AirTransportCompany> l = companyRepo.findAll();
        final List<AirTransportCompany> m = new ArrayList<>();
        for (AirTransportCompany i : l) {
            m.add(i);
        }
        return m;
    }
}