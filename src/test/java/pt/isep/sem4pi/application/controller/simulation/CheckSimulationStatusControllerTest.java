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

/**
 * Testes da consulta do resumo gerado pela US111.
 * Garantem que o menu recebe um DTO simples ou uma mensagem clara.
 */
public class CheckSimulationStatusControllerTest {

    /**
     * Devolve um DTO pronto para apresentar ao utilizador.
     */
    @Test
    public void returnsReportDtoForMenuPresentation() {
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

    /**
     * Informa que o resumo ainda não existe, por exemplo durante a execução.
     */
    @Test
    public void reportsThatSummaryIsNotAvailableYet() {
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

    /**
     * Monta o controller com repositórios simulados para isolar o teste.
     */
    private CheckSimulationStatusController controller(
            final SimulationReportRepository reports) {
        return new CheckSimulationStatusController(
                mock(AuthorizationService.class),
                mock(SimulationRepository.class),
                reports,
                new SimulationSummaryReportDTOMapper());
    }

    /**
     * Cria um relatório simples para testar a conversão para DTO.
     */
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
