package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemorySimulationRepository implements SimulationRepository {

    private final Map<String, Simulation> bySimulationId = new ConcurrentHashMap<>();

    @Override
    public Simulation save(final Simulation simulation) {
        bySimulationId.put(simulation.simulationId(), simulation);
        return simulation;
    }

    @Override
    public Optional<Simulation> ofIdentity(final String simulationId) {
        return Optional.ofNullable(bySimulationId.get(simulationId));
    }

    @Override
    public boolean existsBySimulationId(final String simulationId) {
        return bySimulationId.containsKey(simulationId);
    }

    @Override
    public List<Simulation> findAll() {
        return bySimulationId.values().stream().collect(Collectors.toList());
    }
}