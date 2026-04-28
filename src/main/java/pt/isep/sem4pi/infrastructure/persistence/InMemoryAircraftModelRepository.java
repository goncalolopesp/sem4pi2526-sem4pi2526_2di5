package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

public class InMemoryAircraftModelRepository extends InMemoryDomainRepository<AircraftModel, AircraftModelId> implements AircraftModelRepository {
}
