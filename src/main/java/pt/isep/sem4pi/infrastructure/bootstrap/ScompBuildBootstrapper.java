package pt.isep.sem4pi.infrastructure.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScompBuildBootstrapper {

    private static final Path SCOMP_DIR = Path.of("scomp");
    private static final Path VALIDATOR_BIN = SCOMP_DIR.resolve("flight_test_validator");

    public void execute() {
        if (Files.isExecutable(VALIDATOR_BIN)) {
            return;
        }

        System.out.println("Building SCOMP flight test validator...");
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("make", "flight_test_validator");
            processBuilder.directory(SCOMP_DIR.toFile());
            processBuilder.redirectErrorStream(true);

            final Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("   " + line);
                }
            }

            final int exitCode = process.waitFor();
            if (exitCode != 0 || !Files.isExecutable(VALIDATOR_BIN)) {
                System.out.println("   -> Failed to build flight_test_validator (exit code " + exitCode + ").");
                System.out.println("   -> Run manually: cd scomp && make");
            } else {
                System.out.println("   -> Built flight_test_validator successfully.");
            }
        } catch (final IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.out.println("   -> Could not build flight_test_validator: " + ex.getMessage());
            System.out.println("   -> Run manually: cd scomp && make");
        }
    }
}
