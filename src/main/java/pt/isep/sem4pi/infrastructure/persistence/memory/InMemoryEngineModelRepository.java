package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

public class InMemoryEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId>
        implements EngineModelRepository {

    @Override
    public boolean existsWithManufacturerAndName(ManufacturerId manufacturerId, String name) {
        for (EngineModel engineModel : findAll()) {
            if (engineModel.hasManufacturerAndName(manufacturerId, name)) {
                return true;
            }
        }
        return false;
    }
}