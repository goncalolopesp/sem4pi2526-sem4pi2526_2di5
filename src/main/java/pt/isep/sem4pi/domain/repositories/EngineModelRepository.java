package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.aircraftModel.EngineModel;
import pt.isep.sem4pi.domain.aircraftModel.EngineModelId;

public interface EngineModelRepository extends DomainRepository<EngineModelId, EngineModel> {
}
