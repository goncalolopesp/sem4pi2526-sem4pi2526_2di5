package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.simulation.Simulation;

import java.util.List;
import java.util.Optional;

public interface SimulationRepository {

    Simulation save(Simulation simulation);

    Optional<Simulation> ofIdentity(String simulationId);

    boolean existsBySimulationId(String simulationId);

    List<Simulation> findAll();
}