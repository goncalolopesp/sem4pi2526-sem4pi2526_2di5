package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.Simulation;

/**
 * Define a forma de executar uma simulação.
 * A aplicação não precisa de conhecer os detalhes do programa SCOMP.
 */
public interface SimulationRunner {

    /**
     * Executa o simulador e devolve o resultado.
     */
    SimulationExecutionResult run(Simulation simulation);
}
