package pt.isep.sem4pi.domain.model.simulation.DTO;

public record SimulationStatusDTO(
        String simulationId,
        String status,
        String failureReason,
        SimulationSummaryReportDTO report) {
}
