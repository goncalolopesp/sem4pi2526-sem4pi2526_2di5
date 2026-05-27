package pt.isep.sem4pi.presentation.simulation;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.domain.model.simulation.Simulation;

public class CheckSimulationStatusUI extends AbstractUI {

    @Override
    protected boolean doShow() {

        final String simulationId =
                Console.readNonEmptyLine(
                        "Simulation id:",
                        "Simulation id required");

        final Simulation simulation =
                PersistenceContext.repositories()
                        .simulations()
                        .ofIdentity(simulationId)
                        .orElse(null);

        if (simulation == null) {
            System.out.println("Simulation not found");
            return false;
        }

        System.out.println();
        System.out.println("Simulation:");
        System.out.println(simulation.simulationId());

        System.out.println("Status:");
        System.out.println(simulation.status());

        return true;
    }

    @Override
    public String headline() {
        return "Check Simulation Status";
    }
}