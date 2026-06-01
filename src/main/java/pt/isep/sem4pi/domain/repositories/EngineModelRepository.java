package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

public interface EngineModelRepository extends DomainRepository<EngineModelId, EngineModel> {
    boolean existsWithManufacturerAndName(ManufacturerId manufacturerId, String name);
}