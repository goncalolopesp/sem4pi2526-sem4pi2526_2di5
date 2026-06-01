package pt.isep.sem4pi.application.service.aircraft;

import pt.isep.sem4pi.application.viadto.AircraftModelDTORepresentationBuilder;
import pt.isep.sem4pi.domain.aircraft.Aircraft;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.AircraftModelDTO;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddAircraftService {

    private final AirTransportCompanyRepository companyRepo;
    private final AircraftModelRepository modelRepo;

    public AddAircraftService(AirTransportCompanyRepository companyRepo, AircraftModelRepository modelRepo) {
        this.companyRepo = companyRepo;
        this.modelRepo = modelRepo;
    }

    public void addAircraftToFleet(String companyIataStr, String regStr, String manufacturerStr, String modelNameStr,
                                   String country, int eco, int bus, int first) {

        IataCodeCompany companyIata = new IataCodeCompany(companyIataStr);
        AircraftRegistration registration = new AircraftRegistration(regStr);
        CabinConfiguration seats = new CabinConfiguration(eco, bus, first);

        AirTransportCompany company = companyRepo.findByIataCode(companyIata)
                .orElseThrow(() -> new IllegalArgumentException("Company not found."));

        AircraftModelId realId = new AircraftModelId(new ManufacturerId(manufacturerStr), modelNameStr);
        AircraftModel model = modelRepo.ofIdentity(realId)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft Model not found in system."));

        AircraftModelDTORepresentationBuilder builder = new AircraftModelDTORepresentationBuilder();
        AircraftModelDTO modelDto = model.buildRepresentation(builder);

        if (seats.getTotalCapacity() > modelDto.getMaximumCapacity()) {
            throw new IllegalStateException("Total seats exceed the maximum capacity for model " + modelNameStr);
        }

        if (companyRepo.existsByAircraftRegistration(registration)) {
            throw new IllegalStateException("An aircraft with this registration already exists.");
        }

        Aircraft newAircraft = new Aircraft(registration, realId, seats, country, LocalDate.now());
        company.addAircraftToFleet(newAircraft);

        companyRepo.save(company);
    }

    public List<AirTransportCompany> getAirTransportCompanyList()
    {
        Iterable<AirTransportCompany> l = companyRepo.findAll();
        List<AirTransportCompany> m = new ArrayList<>();

        for (AirTransportCompany i : l)
            m.add(i);

        return m;
    }
}