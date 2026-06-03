package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemorySimulationReportRepository implements SimulationReportRepository {

    private final Map<String, SimulationReport> byReportId = new ConcurrentHashMap<>();
    private final Map<String, SimulationReport> bySimulationId = new ConcurrentHashMap<>();

    @Override
    public SimulationReport save(final SimulationReport report) {
        byReportId.put(report.reportId(), report);
        bySimulationId.put(report.simulationId(), report);
        return report;
    }

    @Override
    public Optional<SimulationReport> ofIdentity(final String reportId) {
        return Optional.ofNullable(byReportId.get(reportId));
    }

    @Override
    public Optional<SimulationReport> findBySimulationId(final String simulationId) {
        return Optional.ofNullable(bySimulationId.get(simulationId));
    }

    @Override
    public List<SimulationReport> findAll() {
        return byReportId.values().stream().collect(Collectors.toList());
    }
}