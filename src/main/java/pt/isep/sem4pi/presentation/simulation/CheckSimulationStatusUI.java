package pt.isep.sem4pi.presentation.simulation;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.simulation.CheckSimulationStatusController;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationStatusDTO;

public class CheckSimulationStatusUI extends AbstractUI {

    private final CheckSimulationStatusController controller =
            new CheckSimulationStatusController();

    @Override
    protected boolean doShow() {
        try {
            final String simulationId =
                    Console.readNonEmptyLine(
                            "Simulation id:",
                            "Simulation id required");
            final SimulationStatusDTO simulation = controller.statusOf(simulationId);

            System.out.println();
            System.out.println("Simulation: " + simulation.simulationId());
            System.out.println("Status: " + simulation.status());
            if (simulation.failureReason() != null) {
                System.out.println("Failure reason: " + simulation.failureReason());
            }

            if (simulation.report() != null) {
                System.out.println("Validation: "
                        + simulation.report().validationResult());
                System.out.println("Total flights: "
                        + simulation.report().totalFlights());
                System.out.println("Summary report: "
                        + simulation.report().fileLocation());
            }
            return true;
        } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Check Simulation Status";
    }
}
