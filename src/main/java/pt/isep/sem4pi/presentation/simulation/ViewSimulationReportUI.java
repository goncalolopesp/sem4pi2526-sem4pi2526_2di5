package pt.isep.sem4pi.presentation.simulation;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.simulation.CheckSimulationStatusController;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationSummaryReportDTO;

public class ViewSimulationReportUI extends AbstractUI {

    private final CheckSimulationStatusController controller =
            new CheckSimulationStatusController();

    @Override
    protected boolean doShow() {
        try {
            final String simulationId = Console.readNonEmptyLine(
                    "Simulation id:",
                    "Simulation id required");
            final SimulationSummaryReportDTO report =
                    controller.reportOf(simulationId);

            System.out.println();
            System.out.println("Simulation: " + report.simulationId());
            System.out.println("Generated at: " + report.generatedAt());
            System.out.println("Total flights: " + report.totalFlights());
            System.out.println();
            System.out.println("Flight execution statuses:");
            for (final SimulationSummaryReportDTO.FlightExecutionDTO flight
                    : report.flights()) {
                System.out.println("- " + flight.flightId()
                        + ": " + flight.executionStatus());
            }

            System.out.println();
            System.out.println("Safety violations: "
                    + report.safetyViolations().size());
            if (report.safetyViolations().isEmpty()) {
                System.out.println("- None");
            } else {
                for (final SimulationSummaryReportDTO.SafetyViolationDTO violation
                        : report.safetyViolations()) {
                    System.out.println("- timestamp=" + violation.timestamp()
                            + ", type=" + violation.type()
                            + ", position=" + violation.position());
                }
            }

            System.out.println();
            System.out.println("Scheduled flights plan validation: "
                    + report.validationResult());
            System.out.println("Report file: " + report.fileLocation());
            return true;
        } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "View Simulation Report";
    }
}
