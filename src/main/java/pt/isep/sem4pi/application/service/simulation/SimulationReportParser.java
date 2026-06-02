package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.SimulationReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

public class SimulationReportParser {

    public SimulationReport parse(final String simulationId,
                                  final String reportPath) throws IOException {

        final String content = Files.readString(Path.of(reportPath));

        final Integer totalFlights = extractTotalFlights(content);
        final String finalResult = extractFinalResult(content);

        return new SimulationReport(
                "REP-" + UUID.randomUUID(),
                simulationId,
                totalFlights,
                finalResult,
                LocalDateTime.now(),
                reportPath
        );
    }

    private Integer extractTotalFlights(final String content) {
        for (final String line : content.split("\\R")) {
            if (line.startsWith("Total flights:")) {
                return Integer.parseInt(line.replace("Total flights:", "").trim());
            }
        }

        return 0;
    }

    private String extractFinalResult(final String content) {
        for (final String line : content.split("\\R")) {
            if (line.startsWith("Simulation result:")) {
                return line.replace("Simulation result:", "").trim();
            }
        }

        return "UNKNOWN";
    }
}