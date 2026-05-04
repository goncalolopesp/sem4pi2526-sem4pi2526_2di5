package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.Manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

public interface EngineModelRepository extends DomainRepository<EngineModelId, EngineModel> {
    default boolean existsWithManufacturerAndName(final ManufacturerId manufacturerId, final String name) {
        for (final EngineModel engineModel : findAll()) {
            if (engineModel.hasManufacturerAndName(manufacturerId, name)) {
                return true;
            }
        }
        return false;
    }
}
