package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.FlightExecutionSummary;
import pt.isep.sem4pi.domain.model.simulation.SafetyViolationSummary;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;

public class SimulationSummaryReportExporter {

    // DEFESA:
    // Esta classe tem alta coesão: conhece apenas nome, conteúdo e escrita do relatório US111.
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private final Path reportsDirectory;

    public SimulationSummaryReportExporter() {
        this(Path.of("reports", "simulation"));
    }

    public SimulationSummaryReportExporter(final Path reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public Path reportPath(final String simulationId,
                           final java.time.LocalDateTime generatedAt) {
        final String safeSimulationId =
                simulationId.replaceAll("[^A-Za-z0-9._-]", "_");
        return reportsDirectory.resolve(
                safeSimulationId + "-" + FILE_TIMESTAMP.format(generatedAt) + ".txt");
    }

    public void export(final SimulationReport report) throws IOException {
        final Path path = Path.of(report.fileLocation());
        final Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        final Path temporaryFile = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            // DEFESA:
            // A escrita temporária seguida de move reduz o risco de deixar um relatório parcialmente escrito.
            Files.writeString(temporaryFile, content(report), StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (final java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    public void delete(final SimulationReport report) throws IOException {
        Files.deleteIfExists(Path.of(report.fileLocation()));
    }

    String content(final SimulationReport report) {
        final StringBuilder content = new StringBuilder();
        content.append("US111 Simulation Summary Report\n");
        content.append("Simulation id: ").append(report.simulationId()).append('\n');
        content.append("Generated at: ").append(report.generatedAt()).append('\n');
        content.append("Total flights: ").append(report.totalFlights()).append("\n\n");
        content.append("Flight execution statuses:\n");

        for (final FlightExecutionSummary flight : report.flightSummaries()) {
            content.append("- ").append(flight.flightId())
                    .append(": ").append(flight.executionStatus()).append('\n');
        }

        content.append("\nSafety violations: ")
                .append(report.safetyViolations().size()).append('\n');
        if (report.safetyViolations().isEmpty()) {
            content.append("- None\n");
        } else {
            for (final SafetyViolationSummary violation : report.safetyViolations()) {
                content.append("- timestamp=").append(violation.timestamp())
                        .append(", type=").append(violation.type())
                        .append(", position=").append(violation.position())
                        .append(", details=").append(violation.details())
                        .append('\n');
            }
        }

        content.append("\nScheduled flights plan validation: ")
                .append(report.finalValidationResult()).append('\n');
        return content.toString();
    }
}
