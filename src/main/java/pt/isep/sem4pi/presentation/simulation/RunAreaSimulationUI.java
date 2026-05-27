package pt.isep.sem4pi.presentation.simulation;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.simulation.RunAreaSimulationController;
import pt.isep.sem4pi.application.service.simulation.SimulationExecutionResult;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class RunAreaSimulationUI extends AbstractUI {

    private final RunAreaSimulationController controller =
            new RunAreaSimulationController();

    @Override
    protected boolean doShow() {

        try {

            final String areaId =
                    Console.readNonEmptyLine(
                            "Air Control Area Id:",
                            "Area id required");

            final Integer timeStep =
                    Integer.parseInt(
                            Console.readNonEmptyLine(
                                    "Time step:",
                                    "Time step required"));

            final Double threshold =
                    Double.parseDouble(
                            Console.readNonEmptyLine(
                                    "Safety threshold:",
                                    "Threshold required"));

            final String flightPlansRaw =
                    Console.readNonEmptyLine(
                            "Flight plan ids (comma separated):",
                            "Flight plans required");

            final List<String> flightPlans =
                    Arrays.stream(flightPlansRaw.split(","))
                            .map(String::trim)
                            .toList();

            final LocalDateTime start =
                    LocalDateTime.now();

            final LocalDateTime end =
                    start.plusMinutes(20);

            final SimulationDTO dto =
                    controller.createSimulation(
                            areaId,
                            start,
                            end,
                            timeStep,
                            threshold,
                            flightPlans);

            controller.submitSimulation(dto.getSimulationId());

            System.out.println();
            System.out.println("Simulation submitted successfully");
            System.out.println("Simulation id:");
            System.out.println(dto.getSimulationId());
            System.out.println("Status:");
            System.out.println("RUNNING");

            return true;

        } catch (Exception e) {
            System.out.println("Error running simulation: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Run Area Simulation";
    }
}