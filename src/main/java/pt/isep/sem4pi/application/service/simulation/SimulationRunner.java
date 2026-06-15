package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.Simulation;

public interface SimulationRunner {

    // DEFESA:
    // Protected Variations: a aplicação depende deste contrato e não de ProcessBuilder ou do binário C.
    SimulationExecutionResult run(Simulation simulation);
}
