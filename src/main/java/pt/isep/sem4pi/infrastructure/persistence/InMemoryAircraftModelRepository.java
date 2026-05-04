package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

import java.util.stream.StreamSupport;

public class InMemoryAircraftModelRepository extends InMemoryDomainRepository<AircraftModel, AircraftModelId> implements AircraftModelRepository {

    @Override
    public boolean containsOfManufacturerAndName(ManufacturerId manufacturerId, String modelName) {
        return StreamSupport.stream(this.findAll().spliterator(), false)
                .anyMatch(a -> a.manufacturerId().equals(manufacturerId) && a.modelName().equals(modelName));
    }
}