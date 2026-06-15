package pt.isep.sem4pi.application.service.simulation;

import eapli.framework.domain.repositories.TransactionalContext;
import pt.isep.sem4pi.domain.model.simulation.ReportFilePath;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationValidationResult;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

public class GenerateSimulationSummaryReportService {

    // DEFESA:
    // Este Application Service orquestra parser, domínio, ficheiro e persistência sem concentrar essas implementações.
    private final RepositoryFactory repositoryFactory;
    private final SimulationReportParser parser;
    private final SimulationSummaryReportExporter exporter;
    private final Clock clock;

    public GenerateSimulationSummaryReportService(
            final RepositoryFactory repositoryFactory,
            final SimulationReportParser parser,
            final SimulationSummaryReportExporter exporter) {
        this(repositoryFactory, parser, exporter, Clock.systemDefaultZone());
    }

    GenerateSimulationSummaryReportService(
            final RepositoryFactory repositoryFactory,
            final SimulationReportParser parser,
            final SimulationSummaryReportExporter exporter,
            final Clock clock) {
        this.repositoryFactory = repositoryFactory;
        this.parser = parser;
        this.exporter = exporter;
        this.clock = clock;
    }

    public SimulationReport generate(final String simulationId,
                                     final String scompReportPath) throws IOException {
        final ParsedSimulationReport parsed = parser.parse(scompReportPath);
        final LocalDateTime generatedAt = LocalDateTime.now(clock);
        final SimulationReport report = new SimulationReport(
                "REP-" + UUID.randomUUID(),
                simulationId,
                generatedAt,
                new ReportFilePath(
                        exporter.reportPath(simulationId, generatedAt).toString()),
                parsed.flightSummaries(),
                parsed.safetyViolations(),
                parsed.validationResult());

        // DEFESA:
        // O ficheiro é criado antes da transação; se a BD falhar existe uma compensação explícita.
        exporter.export(report);

        TransactionalContext tx = null;
        try {
            // DEFESA:
            // O TransactionalContext garante atomicidade entre SimulationReport e o estado final de Simulation.
            tx = repositoryFactory.newTransactionalContext();
            tx.beginTransaction();
            final SimulationRepository simulations = repositoryFactory.simulations(tx);
            final SimulationReportRepository reports =
                    repositoryFactory.simulationReports(tx);
            final Simulation simulation = simulations.ofIdentity(simulationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Simulation not found: " + simulationId));

            reports.save(report);
            updateSimulationStatus(simulation, report.validationResult());
            simulations.save(simulation);
            tx.commit();
            return report;
        } catch (final RuntimeException | Error e) {
            if (tx != null) {
                rollback(tx);
            }
            deleteExportedReport(report, e);
            throw e;
        } finally {
            close(tx);
        }
    }

    private void updateSimulationStatus(
            final Simulation simulation,
            final SimulationValidationResult validationResult) {
        if (validationResult == SimulationValidationResult.PASS) {
            simulation.markAsFinished();
        } else {
            simulation.markAsFailed(
                    "Scheduled flights plan failed simulation validation");
        }
    }

    private void rollback(final TransactionalContext tx) {
        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } catch (final RuntimeException ignored) {
            // Preserve the original failure.
        }
    }

    private void close(final TransactionalContext tx) {
        if (tx == null) {
            return;
        }
        try {
            tx.close();
        } catch (final RuntimeException ignored) {
            // Transaction outcome has already been decided.
        }
    }

    private void deleteExportedReport(
            final SimulationReport report,
            final Throwable originalFailure) {
        try {
            exporter.delete(report);
        } catch (final IOException cleanupFailure) {
            originalFailure.addSuppressed(cleanupFailure);
        }
    }
}
