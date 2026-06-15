package pt.isep.sem4pi.application.service.simulation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimulationReportParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final SimulationReportParser parser = new SimulationReportParser();

    @Test
    public void parsesScompSeparationViolationWithBothPositions() throws Exception {
        // DEFESA:
        // Este teste fixa o contrato do Anti-Corruption Layer para violações com dois voos e duas posições.
        final ParsedSimulationReport report = parser.parse(writeReport(
                "=== US102 Safety Violations (1 shown / 1 total) ===\n"
                        + "  t=7 SEPARATION FLIGHT-A(pid=101) <-> "
                        + "FLIGHT-B(pid=102) dist=2.4km (min=3.0km)\n"
                        + "==========================================\n"));

        assertEquals(2, report.flightSummaries().size());
        assertEquals(1, report.safetyViolations().size());
        assertEquals("SEPARATION", report.safetyViolations().get(0).type());
        assertTrue(report.safetyViolations().get(0).position()
                .contains("FLIGHT-A: lat=41.100000, lon=-8.600000, alt=500.00m"));
        assertTrue(report.safetyViolations().get(0).position()
                .contains("FLIGHT-B: lat=40.900000, lon=-8.200000, alt=520.00m"));
    }

    @Test
    public void rejectsReportWhenScompOmittedViolationDetails() throws Exception {
        // DEFESA:
        // O teste negativo impede que um ficheiro truncado seja aceite como resumo operacional consistente.
        try {
            parser.parse(writeReport(
                    "=== US102 Safety Violations (1 shown / 2 total) ===\n"
                            + "  t=7 ALTITUDE flight=FLIGHT-A pid=101 "
                            + "alt=250.0m (min=300.0m)\n"
                            + "==========================================\n"));
            fail("Expected an incomplete report to be rejected");
        } catch (final IOException expected) {
            assertEquals("SCOMP report omitted safety violation details",
                    expected.getMessage());
        }
    }

    @Test
    public void rejectsReportWhenFlightStatusCountDoesNotMatchTotal() throws Exception {
        // DEFESA:
        // Esta inconsistência protege a invariante entre total declarado e estados efetivamente interpretados.
        final String content = baseReport(
                "=== US102 Safety Violations (0 shown / 0 total) ===\n"
                        + "==========================================\n")
                .replace("Total flights: 2", "Total flights: 3");

        try {
            parser.parse(write("bad-total.txt", content));
            fail("Expected inconsistent flight count to be rejected");
        } catch (final IOException expected) {
            assertEquals("SCOMP report flight count does not match flight statuses",
                    expected.getMessage());
        }
    }

    private String writeReport(final String violationSection) throws Exception {
        return write("scomp-report.txt", baseReport(violationSection));
    }

    private String write(final String filename, final String content) throws Exception {
        final Path file = temporaryFolder.newFile(filename).toPath();
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toString();
    }

    private String baseReport(final String violationSection) {
        return "US109 Simulation Report\n"
                + "Total flights: 2\n\n"
                + "Flight FLIGHT-A, PID 101, updates 1\n"
                + "  t=7 lat=41.100000 lon=-8.600000 alt=500.00 status=1\n\n"
                + "Flight FLIGHT-B, PID 102, updates 1\n"
                + "  t=7 lat=40.900000 lon=-8.200000 alt=520.00 status=1\n\n"
                + "=== US101 Position Tracker (2 entries) ===\n"
                + "  [FLIGHT-A] pid=101 final_pos=(41.1, -8.6, 500.0m) "
                + "status=FINISHED history=1 pts\n"
                + "  [FLIGHT-B] pid=102 final_pos=(40.9, -8.2, 520.0m) "
                + "status=FINISHED history=1 pts\n"
                + "==========================================\n\n"
                + violationSection
                + "\n=== FINAL VALIDATION RESULT ===\n"
                + "Simulation result: PASS\n"
                + "================================\n";
    }
}
