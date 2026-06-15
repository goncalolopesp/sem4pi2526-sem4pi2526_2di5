package pt.isep.sem4pi.application.viadto;

import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationSummaryReportDTO;

/**
 * Prepara os dados do relatório para serem apresentados.
 * Assim, a interface não usa diretamente a entidade de domínio.
 */
public class SimulationSummaryReportDTOMapper {

    /**
     * Copia para o DTO apenas os dados que serão apresentados.
     */
    public SimulationSummaryReportDTO toDTO(final SimulationReport report) {
        return new SimulationSummaryReportDTO(
                report.reportId(),
                report.simulationId(),
                report.generatedAt(),
                report.totalFlights(),
                report.flightSummaries().stream()
                        .map(flight -> new SimulationSummaryReportDTO.FlightExecutionDTO(
                                flight.flightId(),
                                flight.executionStatus()))
                        .toList(),
                report.safetyViolations().stream()
                        .map(violation -> new SimulationSummaryReportDTO.SafetyViolationDTO(
                                violation.timestamp(),
                                violation.type(),
                                violation.position(),
                                violation.details()))
                        .toList(),
                report.finalValidationResult(),
                report.fileLocation());
    }
}
