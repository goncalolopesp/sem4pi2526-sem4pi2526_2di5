package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;

/**
 * Repository interface for managing AircraftModel entities.
 * <p>
 * This interface extends the DomainRepository to provide standard CRUD operations
 * for AircraftModel objects, identified by AircraftModelId. It also includes a custom
 * method to check for uniqueness based on manufacturer and model name.
 * </p>
 */
public interface AircraftModelRepository extends DomainRepository<AircraftModelId, AircraftModel> {
}
