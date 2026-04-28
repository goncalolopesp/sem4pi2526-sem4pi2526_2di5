package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModelId;

public interface AircraftModelRepository extends DomainRepository<AircraftModelId, AircraftModel> {
}
