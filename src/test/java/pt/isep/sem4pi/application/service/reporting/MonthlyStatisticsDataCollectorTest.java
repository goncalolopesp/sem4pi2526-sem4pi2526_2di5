package pt.isep.sem4pi.application.service.reporting;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightStatus;
import pt.isep.sem4pi.domain.model.simulation.SimulatedFlightInput;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationStatus;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MonthlyStatisticsDataCollectorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void collectsOnlyStatisticsForSelectedMonth() throws Exception {
        final Simulation juneSimulation = mock(Simulation.class);
        when(juneSimulation.startTime()).thenReturn(LocalDateTime.of(2026, 6, 3, 10, 0));
        when(juneSimulation.status()).thenReturn(SimulationStatus.FINISHED);
        when(juneSimulation.simulatedFlightInputs()).thenReturn(
                Set.of(mock(SimulatedFlightInput.class), mock(SimulatedFlightInput.class)));

        final Simulation maySimulation = mock(Simulation.class);
        when(maySimulation.startTime()).thenReturn(LocalDateTime.of(2026, 5, 31, 23, 0));
        when(maySimulation.status()).thenReturn(SimulationStatus.FAILED);
        when(maySimulation.simulatedFlightInputs()).thenReturn(Set.of());

        final var simulationRepository = mock(SimulationRepository.class);
        when(simulationRepository.findAll()).thenReturn(
                List.of(juneSimulation, maySimulation));

        final var reportFile = temporaryFolder.newFile("simulation-report.txt").toPath();
        Files.writeString(reportFile,
                "=== US102 Safety Violations (10 shown / 14 total) ===");
        final SimulationReport juneReport = new SimulationReport(
                "REP-1", "SIM-1", 2, "PASS",
                LocalDateTime.of(2026, 6, 3, 11, 0),
                reportFile.toString());
        final SimulationReport mayReport = new SimulationReport(
                "REP-2", "SIM-2", 3, "PASS",
                LocalDateTime.of(2026, 5, 30, 11, 0),
                reportFile.toString());
        final var reportRepository = mock(SimulationReportRepository.class);
        when(reportRepository.findAll()).thenReturn(List.of(juneReport, mayReport));

        final Flight juneFlight = mock(Flight.class);
        when(juneFlight.departureDate()).thenReturn(LocalDate.of(2026, 6, 15));
        when(juneFlight.status()).thenReturn(FlightStatus.PENDING);
        final Flight julyFlight = mock(Flight.class);
        when(julyFlight.departureDate()).thenReturn(LocalDate.of(2026, 7, 1));
        when(julyFlight.status()).thenReturn(FlightStatus.CANCELLED);
        final var flightRepository = mock(FlightRepository.class);
        when(flightRepository.findAll()).thenReturn(List.of(juneFlight, julyFlight));

        final MonthlyStatisticsData data = new MonthlyStatisticsDataCollector(
                simulationRepository, reportRepository, flightRepository)
                .collect(YearMonth.of(2026, 6));

        assertEquals(1, data.simulations());
        assertEquals(Long.valueOf(1), data.simulationsByStatus().get(SimulationStatus.FINISHED));
        assertEquals(2, data.simulatedFlightInputs());
        assertEquals(1, data.simulationReports());
        assertEquals(2, data.reportedFlights());
        assertEquals(14, data.safetyViolations());
        assertEquals(1, data.scheduledFlights());
        assertEquals(Long.valueOf(1), data.flightsByStatus().get(FlightStatus.PENDING));
    }

    @Test
    public void returnsZeroStatisticsWhenMonthHasNoData() {
        final var simulationRepository = mock(SimulationRepository.class);
        final var reportRepository = mock(SimulationReportRepository.class);
        final var flightRepository = mock(FlightRepository.class);
        when(simulationRepository.findAll()).thenReturn(List.of());
        when(reportRepository.findAll()).thenReturn(List.of());
        when(flightRepository.findAll()).thenReturn(List.of());

        final MonthlyStatisticsData data = new MonthlyStatisticsDataCollector(
                simulationRepository, reportRepository, flightRepository)
                .collect(YearMonth.of(2026, 6));

        assertEquals(0, data.simulations());
        assertEquals(0, data.simulationReports());
        assertEquals(0, data.safetyViolations());
        assertEquals(0, data.scheduledFlights());
    }
}
