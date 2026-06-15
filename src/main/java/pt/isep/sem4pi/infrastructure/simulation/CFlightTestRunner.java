package pt.isep.sem4pi.infrastructure.simulation;

import pt.isep.sem4pi.application.service.flightplan.FlightTestExecutionResult;
import pt.isep.sem4pi.application.service.flightplan.FlightTestRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CFlightTestRunner implements FlightTestRunner {

    private static final String TEST_VALIDATOR_PATH = "scomp/flight_test_validator";
    private static final String REPORT_BASE_PATH = "scomp/reports";

    @Override
    public FlightTestExecutionResult run(final String jsonPlanPath,
                                         final double minAltitudeMeters,
                                         final double minReserveFuelKg,
                                         final int stepSeconds,
                                         final String reportPath) {
        try {
            Files.createDirectories(Path.of(REPORT_BASE_PATH));

            final List<String> command = new ArrayList<>();
            command.add(TEST_VALIDATOR_PATH);
            command.add("--plan");
            command.add(jsonPlanPath);
            command.add("--min-alt");
            command.add(String.valueOf(minAltitudeMeters));
            command.add("--min-reserve-fuel");
            command.add(String.valueOf(minReserveFuelKg));
            command.add("--step");
            command.add(String.valueOf(stepSeconds));
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
            return new FlightTestExecutionResult(exitCode == 0, reportPath, output.toString());

        } catch (final IOException e) {
            final String message = e.getMessage() != null && e.getMessage().contains("No such file or directory")
                    ? "C flight test validator not found. Build it with: cd scomp && make"
                    : e.getMessage();
            return new FlightTestExecutionResult(false, reportPath, message);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new FlightTestExecutionResult(false, reportPath, e.getMessage());
        }
    }
}
