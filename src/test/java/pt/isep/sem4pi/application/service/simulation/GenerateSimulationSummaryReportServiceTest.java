package pt.isep.sem4pi.application.service.simulation;

import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationStatus;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenerateSimulationSummaryReportServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-11T10:15:30Z"),
            ZoneOffset.UTC);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void noViolationsGeneratePassReportWithFlightData() throws Exception {
        final InMemoryRepositoryFactory factory = new InMemoryRepositoryFactory();
        final Simulation simulation = runningSimulation("SIM-PASS");
        factory.simulations().save(simulation);
        final Path source = writeScompReport("pass-source.txt", false);
        final Path reportsDirectory = temporaryFolder.newFolder("pass-reports").toPath();

        final SimulationReport report = service(factory, reportsDirectory)
                .generate(simulation.simulationId(), source.toString());

        assertEquals("PASS", report.finalValidationResult());
        assertEquals(Integer.valueOf(2), report.totalFlights());
        assertEquals(2, report.flightSummaries().size());
        assertEquals(SimulationStatus.FINISHED, simulation.status());
        assertTrue(Files.exists(Path.of(report.fileLocation())));

        final String content = Files.readString(
                Path.of(report.fileLocation()),
                StandardCharsets.UTF_8);
        assertTrue(content.contains("Total flights: 2"));
        assertTrue(content.contains("- FLIGHT-A: FINISHED"));
        assertTrue(content.contains("- FLIGHT-B: FINISHED"));
        assertTrue(content.contains("Scheduled flights plan validation: PASS"));
        assertTrue(factory.simulationReports()
                .findBySimulationId(simulation.simulationId())
                .isPresent());
    }

    @Test
    public void violationsGenerateFailReportWithTimestampAndPosition() throws Exception {
        final InMemoryRepositoryFactory factory = new InMemoryRepositoryFactory();
        final Simulation simulation = runningSimulation("SIM-FAIL");
        factory.simulations().save(simulation);
        final Path source = writeScompReport("fail-source.txt", true);
        final Path reportsDirectory = temporaryFolder.newFolder("fail-reports").toPath();

        final SimulationReport report = service(factory, reportsDirectory)
                .generate(simulation.simulationId(), source.toString());

        assertEquals("FAIL", report.finalValidationResult());
        assertEquals(1, report.safetyViolations().size());
        assertEquals(Integer.valueOf(7),
                report.safetyViolations().get(0).timestamp());
        assertTrue(report.safetyViolations().get(0).position()
                .contains("lat=41.100000, lon=-8.600000, alt=250.00m"));
        assertEquals(SimulationStatus.FAILED, simulation.status());

        final String content = Files.readString(Path.of(report.fileLocation()));
        assertTrue(content.contains("timestamp=7"));
        assertTrue(content.contains("position=FLIGHT-A: lat=41.100000"));
        assertTrue(content.contains("Scheduled flights plan validation: FAIL"));
    }

    @Test
    public void persistenceFailureRollsBackAndDeletesExportedFile() throws Exception {
        final RepositoryFactory factory = mock(RepositoryFactory.class);
        final TransactionalContext tx = mock(TransactionalContext.class);
        final SimulationRepository simulations = mock(SimulationRepository.class);
        final SimulationReportRepository reports =
                mock(SimulationReportRepository.class);
        final Simulation simulation = runningSimulation("SIM-ROLLBACK");
        final Path source = writeScompReport("rollback-source.txt", false);
        final Path reportsDirectory =
                temporaryFolder.newFolder("rollback-reports").toPath();

        when(factory.newTransactionalContext()).thenReturn(tx);
        when(factory.simulations(tx)).thenReturn(simulations);
        when(factory.simulationReports(tx)).thenReturn(reports);
        when(simulations.ofIdentity(simulation.simulationId()))
                .thenReturn(java.util.Optional.of(simulation));
        when(tx.isActive()).thenReturn(true);
        when(reports.save(any(SimulationReport.class)))
                .thenThrow(new IllegalStateException("database unavailable"));

        try {
            service(factory, reportsDirectory)
                    .generate(simulation.simulationId(), source.toString());
            fail("Expected persistence failure");
        } catch (final IllegalStateException expected) {
            assertEquals("database unavailable", expected.getMessage());
        }

        verify(tx).rollback();
        verify(tx).close();
        try (java.util.stream.Stream<Path> files = Files.list(reportsDirectory)) {
            assertFalse(files.findAny().isPresent());
        }
    }

    @Test
    public void simulationSaveFailureRollsBackReportAndDeletesExportedFile()
            throws Exception {
        final RepositoryFactory factory = mock(RepositoryFactory.class);
        final TransactionalContext tx = mock(TransactionalContext.class);
        final SimulationRepository simulations = mock(SimulationRepository.class);
        final SimulationReportRepository reports =
                mock(SimulationReportRepository.class);
        final Simulation simulation = runningSimulation("SIM-SECOND-SAVE-FAIL");
        final Path source = writeScompReport("second-save-source.txt", false);
        final Path reportsDirectory =
                temporaryFolder.newFolder("second-save-reports").toPath();

        when(factory.newTransactionalContext()).thenReturn(tx);
        when(factory.simulations(tx)).thenReturn(simulations);
        when(factory.simulationReports(tx)).thenReturn(reports);
        when(simulations.ofIdentity(simulation.simulationId()))
                .thenReturn(java.util.Optional.of(simulation));
        when(tx.isActive()).thenReturn(true);
        when(simulations.save(simulation))
                .thenThrow(new IllegalStateException("simulation save failed"));

        try {
            service(factory, reportsDirectory)
                    .generate(simulation.simulationId(), source.toString());
            fail("Expected simulation persistence failure");
        } catch (final IllegalStateException expected) {
            assertEquals("simulation save failed", expected.getMessage());
        }

        verify(reports).save(any(SimulationReport.class));
        verify(tx).rollback();
        verify(tx).close();
        try (java.util.stream.Stream<Path> files = Files.list(reportsDirectory)) {
            assertFalse(files.findAny().isPresent());
        }
    }

    @Test
    public void exportFailureDoesNotStartPersistenceTransaction() throws Exception {
        final RepositoryFactory factory = mock(RepositoryFactory.class);
        final Path source = writeScompReport("export-failure-source.txt", false);
        final Path invalidReportsDirectory =
                temporaryFolder.newFile("not-a-directory").toPath();

        try {
            service(factory, invalidReportsDirectory)
                    .generate("SIM-EXPORT-FAIL", source.toString());
            fail("Expected report export failure");
        } catch (final java.nio.file.FileSystemException expected) {
            assertTrue(expected.getMessage().contains("not-a-directory"));
        }

        verify(factory, never()).newTransactionalContext();
    }

    private GenerateSimulationSummaryReportService service(
            final RepositoryFactory factory,
            final Path reportsDirectory) {
        return new GenerateSimulationSummaryReportService(
                factory,
                new SimulationReportParser(),
                new SimulationSummaryReportExporter(reportsDirectory),
                FIXED_CLOCK);
    }

    private Simulation runningSimulation(final String simulationId) {
        final Simulation simulation = new Simulation(
                simulationId,
                "ACA-1",
                LocalDateTime.of(2026, 6, 11, 10, 0),
                LocalDateTime.of(2026, 6, 11, 10, 20),
                1,
                3.0,
                39.0,
                43.0,
                -9.0,
                -3.0,
                0,
                1200,
                "clear",
                3.0,
                300.0,
                10);
        simulation.addFlightPlan("Flight_Plan_A.json");
        simulation.addFlightPlan("Flight_Plan_B.json");
        simulation.markAsRunning();
        return simulation;
    }

    private Path writeScompReport(final String filename,
                                  final boolean withViolation) throws Exception {
        final String violationSection = withViolation
                ? "=== US102 Safety Violations (1 shown / 1 total) ===\n"
                + "  t=7 ALTITUDE  flight=FLIGHT-A pid=101 alt=250.0m (min=300.0m)\n"
                + "==========================================\n"
                : "=== US102 Safety Violations (0 shown / 0 total) ===\n"
                + "==========================================\n";

        final String content =
                "US109 Simulation Report\n"
                + "Total flights: 2\n\n"
                + "Flight FLIGHT-A, PID 101, updates 1\n"
                + "  t=7 lat=41.100000 lon=-8.600000 alt=250.00 status=1\n\n"
                + "Flight FLIGHT-B, PID 102, updates 1\n"
                + "  t=7 lat=40.900000 lon=-8.200000 alt=500.00 status=1\n\n"
                + "=== US101 Position Tracker (2 entries) ===\n"
                + "  [FLIGHT-A] pid=101 final_pos=(41.1, -8.6, 250.0m) status=FINISHED history=1 pts\n"
                + "  [FLIGHT-B] pid=102 final_pos=(40.9, -8.2, 500.0m) status=FINISHED history=1 pts\n"
                + "==========================================\n\n"
                + violationSection
                + "\n=== FINAL VALIDATION RESULT ===\n"
                + "Simulation result: PASS\n"
                + "================================\n";

        final Path file = temporaryFolder.newFile(filename).toPath();
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }
}
