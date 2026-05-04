package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;

public interface AircraftModelRepository extends DomainRepository<AircraftModelId, AircraftModel> {

    /// Checks if the combination of manufacturerID and modelName already exists
    boolean containsOfManufacturerAndName(ManufacturerId manufacturerId, String modelName);
}
