package pt.isep.sem4pi.infrastructure.bootstrap;

import pt.isep.sem4pi.application.service.engineModel.EngineModelRegistrationService;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.math.BigDecimal;

import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;

public class BackofficeConfigurationBootstrapper {
    private final ManufacturerRepository manufacturerRepository = PersistenceContext.repositories().manufacturers();
    private final EngineModelRepository engineModelRepository = PersistenceContext.repositories().engineModels();
    private final EngineModelRegistrationService engineModelRegistrationService =
            new EngineModelRegistrationService(engineModelRepository, manufacturerRepository);

    public boolean execute() {
        registerManufacturer("CFM", "CFM International", "France");
        registerManufacturer("RR", "Rolls-Royce", "United Kingdom");
        registerManufacturer("GE", "General Electric", "United States");

        registerEngineModel("CFM56-5B", "CFM", "CFM56-5B", EngineType.TURBOFAN,
                new BigDecimal("120.00"), FuelType.JET_A1, new BigDecimal("37.50"));
        registerEngineModel("TRENT1000", "RR", "Trent 1000", EngineType.TURBOFAN,
                new BigDecimal("330.00"), FuelType.JET_A1, new BigDecimal("41.20"));
        registerEngineModel("GENX-1B", "GE", "GEnx-1B", EngineType.TURBOFAN,
                new BigDecimal("330.00"), FuelType.JET_A1, new BigDecimal("40.80"));

        return true;
    }

    private void registerManufacturer(final String code, final String name, final String country) {
        final ManufacturerId manufacturerId = new ManufacturerId(code);
        if (!manufacturerRepository.containsOfIdentity(manufacturerId)) {
            manufacturerRepository.save(new Manufacturer(manufacturerId, name, country));
        }
    }

    private void registerEngineModel(final String modelCode, final String manufacturerCode, final String name,
                                     final EngineType type, final BigDecimal power, final FuelType fuelType,
                                     final BigDecimal efficiency) {
        final ManufacturerId manufacturerId = new ManufacturerId(manufacturerCode);
        if (!engineModelRepository.containsOfIdentity(new pt.isep.sem4pi.domain.model.engineModel.EngineModelId(modelCode))
                && !engineModelRepository.existsWithManufacturerAndName(manufacturerId, name)) {
            engineModelRegistrationService.register(modelCode, manufacturerCode, name, type, power, fuelType, efficiency);
        }
    }
}
