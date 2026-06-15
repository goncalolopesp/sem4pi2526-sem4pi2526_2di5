package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.simulation.SimulationReport;

import java.util.List;
import java.util.Optional;

public interface SimulationReportRepository {

    // DEFESA:
    // O Repository oferece uma coleção orientada ao domínio e protege a aplicação da tecnologia de persistência.
    SimulationReport save(SimulationReport report);

    Optional<SimulationReport> ofIdentity(String reportId);

    Optional<SimulationReport> findBySimulationId(String simulationId);

    List<SimulationReport> findAll();
}
