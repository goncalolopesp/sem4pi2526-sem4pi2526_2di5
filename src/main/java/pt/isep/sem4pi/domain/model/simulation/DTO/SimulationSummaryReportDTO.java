package pt.isep.sem4pi.domain.model.simulation.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationSummaryReportDTO(
        String reportId,
        String simulationId,
        LocalDateTime generatedAt,
        int totalFlights,
        List<FlightExecutionDTO> flights,
        List<SafetyViolationDTO> safetyViolations,
        String validationResult,
        String fileLocation) {

    public SimulationSummaryReportDTO {
        flights = List.copyOf(flights);
        safetyViolations = List.copyOf(safetyViolations);
    }

    public record FlightExecutionDTO(String flightId, String executionStatus) {
    }

    public record SafetyViolationDTO(
            int timestamp,
            String type,
            String position,
            String details) {
    }
}
