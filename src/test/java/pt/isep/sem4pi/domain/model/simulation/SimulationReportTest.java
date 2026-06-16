package pt.isep.sem4pi.domain.model.simulation;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testes das regras de domínio do SimulationReport.
 * Mostram quando o resultado final deve passar a FAIL.
 */
public class SimulationReportTest {

    /**
     * Um voo que não terminou com sucesso invalida o relatório.
     */
    @Test
    public void unsuccessfulFlightForcesFail() {
        final SimulationReport report = report(
                List.of(new FlightExecutionSummary("FLIGHT-1", "ABORTED")),
                List.of(),
                SimulationValidationResult.PASS);

        assertEquals(SimulationValidationResult.FAIL, report.validationResult());
    }

    /**
     * Uma violação de segurança invalida o relatório mesmo que a SCOMP indique PASS.
     */
    @Test
    public void safetyViolationForcesFailEvenWhenScompResultIsPass() {
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

    /**
     * Cria relatórios pequenos para testar só a regra de validação.
     */
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
