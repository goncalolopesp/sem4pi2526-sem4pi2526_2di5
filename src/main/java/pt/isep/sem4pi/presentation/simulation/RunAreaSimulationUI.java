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

            final Double minLat = Double.parseDouble(Console.readNonEmptyLine("Minimum latitude:", "Minimum latitude required"));
            final Double maxLat = Double.parseDouble(Console.readNonEmptyLine("Maximum latitude:", "Maximum latitude required"));
            final Double minLon = Double.parseDouble(Console.readNonEmptyLine("Minimum longitude:", "Minimum longitude required"));
            final Double maxLon = Double.parseDouble(Console.readNonEmptyLine("Maximum longitude:", "Maximum longitude required"));

            final Integer startSecond = Integer.parseInt(Console.readNonEmptyLine("Start second:", "Start second required"));
            final Integer endSecond = Integer.parseInt(Console.readNonEmptyLine("End second:", "End second required"));
            final Integer timeStep = Integer.parseInt(Console.readNonEmptyLine("Time step:", "Time step required"));

            final String weather = Console.readNonEmptyLine("Weather (clear/rain/storm/fog):", "Weather required");

            final Double safetyMinSepNm = Double.parseDouble(Console.readNonEmptyLine("Minimum separation (NM):", "Minimum separation required"));
            final Double safetyMinAltM = Double.parseDouble(Console.readNonEmptyLine("Minimum altitude (m):", "Minimum altitude required"));
            final Integer violationThreshold = Integer.parseInt(Console.readNonEmptyLine("Maximum allowed violations:", "Violation threshold required"));

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
                            minLat,
                            maxLat,
                            minLon,
                            maxLon,
                            startSecond,
                            endSecond,
                            timeStep,
                            weather,
                            safetyMinSepNm,
                            safetyMinAltM,
                            violationThreshold,
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