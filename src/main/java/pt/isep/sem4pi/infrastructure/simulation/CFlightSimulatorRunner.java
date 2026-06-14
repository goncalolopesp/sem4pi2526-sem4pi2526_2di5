package pt.isep.sem4pi.infrastructure.simulation;

import pt.isep.sem4pi.application.service.simulation.SimulationExecutionResult;
import pt.isep.sem4pi.application.service.simulation.SimulationRunner;
import pt.isep.sem4pi.domain.model.simulation.SimulatedFlightInput;
import pt.isep.sem4pi.domain.model.simulation.Simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CFlightSimulatorRunner implements SimulationRunner {

    private static final String SIMULATOR_PATH = "scomp/flight_simulator";
    private static final String FLIGHT_PLAN_BASE_PATH = "scomp/data";
    private static final String REPORT_BASE_PATH = "scomp/reports";

    @Override
    public SimulationExecutionResult run(final Simulation simulation) {
        try {
            final Path reportsDir = Path.of(REPORT_BASE_PATH);
            Files.createDirectories(reportsDir);

            final String reportPath = reportsDir
                    .resolve(simulation.simulationId() + ".txt")
                    .toString();

            final List<String> command = new ArrayList<>();

            command.add(SIMULATOR_PATH);

            command.add("--area");
            command.add(String.valueOf(simulation.minLat()));
            command.add(String.valueOf(simulation.maxLat()));
            command.add(String.valueOf(simulation.minLon()));
            command.add(String.valueOf(simulation.maxLon()));

            command.add("--start");
            command.add(String.valueOf(simulation.startSecond()));

            command.add("--end");
            command.add(String.valueOf(simulation.endSecond()));

            command.add("--step");
            command.add(String.valueOf(simulation.timeStep()));

            command.add("--weather");
            command.add(simulation.weather());

            command.add("--safety-min-sep");
            command.add(String.valueOf(simulation.safetyMinSepNm()));

            command.add("--safety-min-alt");
            command.add(String.valueOf(simulation.safetyMinAltM()));

            command.add("--violation-threshold");
            command.add(String.valueOf(simulation.violationThreshold()));

            command.add("--perf-max-flights");
            command.add(String.valueOf(simulation.simulatedFlightInputs().size()));

            for (final SimulatedFlightInput input : simulation.simulatedFlightInputs()) {
                command.add("--flight");
                command.add(Path.of(FLIGHT_PLAN_BASE_PATH, input.flightPlanId()).toString());
            }

            command.add("--output");
            command.add(reportPath);

            final ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            final Process process = processBuilder.start();

            final StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            final int exitCode = process.waitFor();

            return new SimulationExecutionResult(
                    exitCode == 0,
                    reportPath,
                    output.toString()
            );

        } catch (final IOException e) {
            return new SimulationExecutionResult(false, "", e.getMessage());

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new SimulationExecutionResult(false, "", e.getMessage());
        }
    }
}