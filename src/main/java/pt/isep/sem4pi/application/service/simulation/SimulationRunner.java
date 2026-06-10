package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.Simulation;

public interface SimulationRunner {

    SimulationExecutionResult run(Simulation simulation);
}