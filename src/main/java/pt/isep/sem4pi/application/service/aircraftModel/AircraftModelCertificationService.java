package pt.isep.sem4pi.application.service.aircraftModel;

import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

public class AircraftModelCertificationService {
    private final AircraftModelRepository aircraftModelRepository;
    private final EngineModelRepository engineModelRepository;

    public AircraftModelCertificationService(final AircraftModelRepository aircraftModelRepository,
                                             final EngineModelRepository engineModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
        this.engineModelRepository = engineModelRepository;
    }

    /**
     * Certifica um motor num modelo de aeronave, aplicando regras do agregado.
     */
    public AircraftModel certifyEngine(final AircraftModelId aircraftModelId, final EngineModelId engineModelId) {
        final AircraftModel aircraftModel = aircraftModelRepository.ofIdentity(aircraftModelId)
                .orElseThrow(() -> new IllegalArgumentException("The aircraft model does not exist."));
        final EngineModel engineModel = engineModelRepository.ofIdentity(engineModelId)
                .orElseThrow(() -> new IllegalArgumentException("The engine model does not exist."));

        aircraftModel.certifyEngine(engineModel);
        return aircraftModelRepository.save(aircraftModel);
    }
}
