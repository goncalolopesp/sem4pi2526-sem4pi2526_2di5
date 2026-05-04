package pt.isep.sem4pi.application.engineModel;

import pt.isep.sem4pi.domain.model.Manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

import java.math.BigDecimal;

public class EngineModelRegistrationService {
    private final EngineModelRepository engineModelRepository;
    private final ManufacturerRepository manufacturerRepository;

    public EngineModelRegistrationService(final EngineModelRepository engineModelRepository,
                                          final ManufacturerRepository manufacturerRepository) {
        this.engineModelRepository = engineModelRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    public EngineModel register(final String engineModelCode, final String manufacturerCode, final String name,
                                final String type, final BigDecimal power, final String fuelType,
                                final BigDecimal efficiency) {
        final EngineModelId engineModelId = new EngineModelId(engineModelCode);
        final ManufacturerId manufacturerId = new ManufacturerId(manufacturerCode);

        if (!manufacturerRepository.containsOfIdentity(manufacturerId)) {
            throw new IllegalArgumentException("The manufacturer does not exist.");
        }
        if (engineModelRepository.containsOfIdentity(engineModelId)) {
            throw new IllegalArgumentException("The engine model code is already in use.");
        }
        if (engineModelRepository.existsWithManufacturerAndName(manufacturerId, name)) {
            throw new IllegalArgumentException("An engine model with this name already exists for that manufacturer.");
        }

        final EngineModel engineModel =
                new EngineModel(engineModelId, manufacturerId, name, type, power, fuelType, efficiency);
        return engineModelRepository.save(engineModel);
    }
}
