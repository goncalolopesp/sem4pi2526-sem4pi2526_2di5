package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.simulation.SimulationReport;

@FunctionalInterface
public interface SafetyViolationTotalReader {

    int totalOf(SimulationReport report);
}
