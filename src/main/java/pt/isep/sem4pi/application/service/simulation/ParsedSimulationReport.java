package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.FlightExecutionSummary;
import pt.isep.sem4pi.domain.model.simulation.SafetyViolationSummary;
import pt.isep.sem4pi.domain.model.simulation.SimulationValidationResult;

import java.util.List;

public record ParsedSimulationReport(
        List<FlightExecutionSummary> flightSummaries,
        List<SafetyViolationSummary> safetyViolations,
        SimulationValidationResult validationResult) {

    public ParsedSimulationReport {
        flightSummaries = List.copyOf(flightSummaries);
        safetyViolations = List.copyOf(safetyViolations);
    }
}
