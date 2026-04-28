package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.aircraftModel.Manufacturer;
import pt.isep.sem4pi.domain.aircraftModel.ManufacturerId;

public interface ManufacturerRepository extends DomainRepository<ManufacturerId, Manufacturer> {
}
