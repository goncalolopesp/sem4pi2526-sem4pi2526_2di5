package pt.isep.sem4pi.infrastructure.reporting;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pt.isep.sem4pi.domain.model.simulation.FlightExecutionSummary;
import pt.isep.sem4pi.domain.model.simulation.ReportFilePath;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationValidationResult;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimulationReportFileViolationReaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readsTotalFromScompReportFile() throws Exception {
        final var reportFile = temporaryFolder.newFile("simulation-report.txt").toPath();
        Files.writeString(reportFile,
                "=== US102 Safety Violations (10 shown / 14 total) ===");

        final SimulationReport report = reportAt(reportFile.toString());

        assertEquals(14, new SimulationReportFileViolationReader().totalOf(report));
    }

    @Test
    public void returnsZeroWhenFileCannotBeRead() {
        final SimulationReport report = reportAt("missing-report.txt");

        assertEquals(0, new SimulationReportFileViolationReader().totalOf(report));
    }

    private SimulationReport reportAt(final String path) {
        return new SimulationReport(
                "REP-1",
                "SIM-1",
                LocalDateTime.of(2026, 6, 3, 11, 0),
                new ReportFilePath(path),
                List.of(new FlightExecutionSummary("FLIGHT-1", "FINISHED")),
                List.of(),
                SimulationValidationResult.PASS);
    }
}
