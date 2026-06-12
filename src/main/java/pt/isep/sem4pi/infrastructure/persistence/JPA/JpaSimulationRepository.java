package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaSimulationRepository
        extends JpaAutoTxRepository<Simulation, String, String>
        implements SimulationRepository {

    public JpaSimulationRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "simulationId");
    }

    public JpaSimulationRepository(final TransactionalContext autoTx) {
        super(autoTx, "simulationId");
    }

    @Override
    public Simulation save(final Simulation simulation) {
        return super.save(simulation);
    }

    @Override
    public Optional<Simulation> ofIdentity(final String simulationId) {
        return matchOne("e.simulationId = :simulationId", Map.of("simulationId", simulationId));
    }

    @Override
    public boolean existsBySimulationId(final String simulationId) {
        return ofIdentity(simulationId).isPresent();
    }

    @Override
    public List<Simulation> findAll() {
        return (List<Simulation>) match("1=1");
    }
}
