package pt.isep.sem4pi.application.controller.simulation;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.application.viadto.SimulationSummaryReportDTOMapper;
import pt.isep.sem4pi.domain.model.simulation.FlightExecutionSummary;
import pt.isep.sem4pi.domain.model.simulation.ReportFilePath;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationValidationResult;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationSummaryReportDTO;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckSimulationStatusControllerTest {

    @Test
    public void returnsReportDtoForMenuPresentation() {
        // DEFESA:
        // Este teste garante que a fronteira de apresentação recebe DTO e não o Aggregate Root persistente.
        final SimulationReportRepository reports =
                mock(SimulationReportRepository.class);
        final SimulationReport report = report();
        when(reports.findBySimulationId("SIM-MENU"))
                .thenReturn(Optional.of(report));

        final CheckSimulationStatusController controller = controller(reports);
        final SimulationSummaryReportDTO dto = controller.reportOf("SIM-MENU");

        assertEquals("SIM-MENU", dto.simulationId());
        assertEquals(1, dto.totalFlights());
        assertEquals("PASS", dto.validationResult());
        assertEquals("FINISHED", dto.flights().get(0).executionStatus());
    }

    @Test
    public void reportsThatSummaryIsNotAvailableYet() {
        // DEFESA:
        // O cenário assíncrono distingue relatório inexistente de uma simulação necessariamente falhada.
        final SimulationReportRepository reports =
                mock(SimulationReportRepository.class);
        when(reports.findBySimulationId("SIM-RUNNING"))
                .thenReturn(Optional.empty());

        try {
            controller(reports).reportOf("SIM-RUNNING");
            fail("Expected missing report");
        } catch (final IllegalArgumentException expected) {
            assertEquals(
                    "Simulation report not found. The simulation may still be running.",
                    expected.getMessage());
        }
    }

    private CheckSimulationStatusController controller(
            final SimulationReportRepository reports) {
        return new CheckSimulationStatusController(
                mock(AuthorizationService.class),
                mock(SimulationRepository.class),
                reports,
                new SimulationSummaryReportDTOMapper());
    }

    private SimulationReport report() {
        return new SimulationReport(
                "REP-MENU",
                "SIM-MENU",
                LocalDateTime.of(2026, 6, 11, 12, 0),
                new ReportFilePath("reports/simulation/SIM-MENU.txt"),
                List.of(new FlightExecutionSummary("FLIGHT-A", "FINISHED")),
                List.of(),
                SimulationValidationResult.PASS);
    }
}
