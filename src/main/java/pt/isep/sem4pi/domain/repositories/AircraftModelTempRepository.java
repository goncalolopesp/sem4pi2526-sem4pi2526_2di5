package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.aircraft.AircraftModelTemp;

import java.util.Optional;

public interface AircraftModelTempRepository {
    Optional<AircraftModelTemp> findById(String modelId);
    AircraftModelTemp save(AircraftModelTemp model);
}
