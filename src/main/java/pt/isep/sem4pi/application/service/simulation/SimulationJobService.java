package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationJobService {

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private final SimulationRepository simulationRepository;
    private final SimulationReportRepository simulationReportRepository;
    private final SimulationRunner simulationRunner;
    private final SimulationReportParser simulationReportParser;

    public SimulationJobService(final SimulationRepository simulationRepository,
                                final SimulationReportRepository simulationReportRepository,
                                final SimulationRunner simulationRunner,
                                final SimulationReportParser simulationReportParser) {
        this.simulationRepository = simulationRepository;
        this.simulationReportRepository = simulationReportRepository;
        this.simulationRunner = simulationRunner;
        this.simulationReportParser = simulationReportParser;
    }

    public void submit(final String simulationId) {
        EXECUTOR.submit(() -> runSimulation(simulationId));
    }

    private void runSimulation(final String simulationId) {
        final Simulation simulation = simulationRepository.ofIdentity(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found."));

        try {
            simulation.markAsRunning();
            simulationRepository.save(simulation);

            final SimulationExecutionResult result =
                    simulationRunner.run(simulation);

            if (result.success()) {
                final SimulationReport report =
                        simulationReportParser.parse(
                                simulation.simulationId(),
                                result.reportPath());

                simulationReportRepository.save(report);

                if ("FAIL".equalsIgnoreCase(report.finalValidationResult())) {
                    simulation.markAsFailed();
                } else {
                    simulation.markAsFinished();
                }

            } else {
                simulation.markAsFailed();
            }

        } catch (final Exception e) {
            simulation.markAsFailed();
        }

        simulationRepository.save(simulation);
    }
}