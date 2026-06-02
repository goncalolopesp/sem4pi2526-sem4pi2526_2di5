package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

import java.util.stream.StreamSupport;

/**
 * In-memory implementation of the AircraftModelRepository.
 * <p>This class provides an in-memory storage solution for AircraftModel entities,
 * extending the InMemoryDomainRepository. It implements the custom method for
 * checking uniqueness based on manufacturer and model name.</p>
 */
public class InMemoryAircraftModelRepository extends InMemoryDomainRepository<AircraftModel, AircraftModelId> implements AircraftModelRepository {
}