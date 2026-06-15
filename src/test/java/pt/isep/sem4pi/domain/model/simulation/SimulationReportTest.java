package pt.isep.sem4pi.domain.model.simulation;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimulationReportTest {

    @Test
    public void unsuccessfulFlightForcesFail() {
        // DEFESA:
        // Este teste unitário de domínio prova que a regra final pertence ao Aggregate Root.
        final SimulationReport report = report(
                List.of(new FlightExecutionSummary("FLIGHT-1", "ABORTED")),
                List.of(),
                SimulationValidationResult.PASS);

        assertEquals(SimulationValidationResult.FAIL, report.validationResult());
    }

    @Test
    public void safetyViolationForcesFailEvenWhenScompResultIsPass() {
        // DEFESA:
        // Este cenário prova defesa em profundidade: um PASS externo não ultrapassa invariantes do domínio.
        final SimulationReport report = report(
                List.of(new FlightExecutionSummary("FLIGHT-1", "FINISHED")),
                List.of(new SafetyViolationSummary(
                        3,
                        "ALTITUDE",
                        "lat=41.0, lon=-8.0, alt=200m",
                        "below minimum altitude")),
                SimulationValidationResult.PASS);

        assertEquals(SimulationValidationResult.FAIL, report.validationResult());
    }

    private SimulationReport report(
            final List<FlightExecutionSummary> flights,
            final List<SafetyViolationSummary> violations,
            final SimulationValidationResult scompResult) {
        return new SimulationReport(
                "REP-1",
                "SIM-1",
                LocalDateTime.of(2026, 6, 11, 10, 0),
                new ReportFilePath("reports/simulation/report.txt"),
                flights,
                violations,
                scompResult);
    }
}
