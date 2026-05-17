package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

public class InMemoryAircraftModelRepository extends InMemoryDomainRepository<AircraftModel, AircraftModelId> implements AircraftModelRepository {
}
