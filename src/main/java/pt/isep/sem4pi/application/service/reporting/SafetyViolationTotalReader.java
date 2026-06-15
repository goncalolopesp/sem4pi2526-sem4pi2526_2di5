package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.simulation.SimulationReport;

@FunctionalInterface
public interface SafetyViolationTotalReader {

    // DEFESA:
    // Esta abstração impede que o coletor de aplicação dependa de Files, Path e regex de infraestrutura.
    int totalOf(SimulationReport report);
}
