package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulationJobService {

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private final SimulationRepository simulationRepository;
    private final SimulationRunner simulationRunner;
    private final GenerateSimulationSummaryReportService reportService;

    public SimulationJobService(final SimulationRepository simulationRepository,
                                final SimulationRunner simulationRunner,
                                final GenerateSimulationSummaryReportService reportService) {
        this.simulationRepository = simulationRepository;
        this.simulationRunner = simulationRunner;
        this.reportService = reportService;
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
                reportService.generate(simulation.simulationId(), result.reportPath());
                return;
            } else {
                simulation.markAsFailed(result.consoleOutput());
            }

        } catch (final Exception e) {
            if (simulation.status() != pt.isep.sem4pi.domain.model.simulation.SimulationStatus.FAILED) {
                simulation.markAsFailed(e.getMessage());
            }
        }

        simulationRepository.save(simulation);
    }
}
