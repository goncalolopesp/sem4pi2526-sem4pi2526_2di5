package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
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

    /**
     * Checks if an aircraft model with the given manufacturer ID and model name already exists.
     * <p>This implementation iterates through all stored aircraft models to find a match.</p>
     *
     * @param manufacturerId the ID of the manufacturer
     * @param modelName the name of the aircraft model
     * @return true if a model with the specified manufacturer and name exists, false otherwise
     */
    @Override
    public boolean containsOfManufacturerAndName(ManufacturerId manufacturerId, String modelName) {
        return StreamSupport.stream(this.findAll().spliterator(), false)
                .anyMatch(a -> a.manufacturerId().equals(manufacturerId) && a.modelName().equals(modelName));
    }
}