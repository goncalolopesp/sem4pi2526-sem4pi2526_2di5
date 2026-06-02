package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaSimulationReportRepository
        extends JpaAutoTxRepository<SimulationReport, String, String>
        implements SimulationReportRepository {

    public JpaSimulationReportRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "reportId");
    }

    @Override
    public SimulationReport save(final SimulationReport report) {
        return super.save(report);
    }

    @Override
    public Optional<SimulationReport> ofIdentity(final String reportId) {
        return matchOne("e.reportId = :reportId", Map.of("reportId", reportId));
    }

    @Override
    public Optional<SimulationReport> findBySimulationId(final String simulationId) {
        return matchOne("e.simulationId = :simulationId", Map.of("simulationId", simulationId));
    }

    @Override
    public List<SimulationReport> findAll() {
        return (List<SimulationReport>) match("1=1");
    }
}