package pt.isep.sem4pi.infrastructure.reporting;

import pt.isep.sem4pi.application.service.reporting.SafetyViolationTotalReader;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimulationReportFileViolationReader
        implements SafetyViolationTotalReader {

    // DEFESA:
    // Este adaptador contém a variação do formato físico legado, evitando contaminar o coletor mensal.
    private static final Pattern VIOLATION_TOTAL = Pattern.compile(
            "Safety Violations \\(\\d+ shown / (\\d+) total\\)",
            Pattern.CASE_INSENSITIVE);

    @Override
    public int totalOf(final SimulationReport report) {
        try {
            final String content = Files.readString(Path.of(report.fileLocation()));
            final Matcher matcher = VIOLATION_TOTAL.matcher(content);
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
        } catch (final IOException | RuntimeException ignored) {
            return 0;
        }
    }
}
