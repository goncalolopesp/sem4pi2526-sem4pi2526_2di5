package pt.isep.sem4pi.application.service.flightplan;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum FlightPlanFileFormat {

    CORE_FLIGHT_DSL(".dsldig5", "Core Flight DSL (US120)");

    private final String extension;
    private final String description;

    FlightPlanFileFormat(final String extension, final String description) {
        this.extension = extension;
        this.description = description;
    }

    public String extension() {
        return extension;
    }

    public String description() {
        return description;
    }

    public static Optional<FlightPlanFileFormat> fromPath(final Path path) {
        if (path == null || path.getFileName() == null) {
            return Optional.empty();
        }
        final String fileName = path.getFileName().toString().toLowerCase();
        return Arrays.stream(values())
                .filter(format -> fileName.endsWith(format.extension))
                .findFirst();
    }

    public static String supportedFormatsMessage() {
        return Arrays.stream(values())
                .map(format -> format.extension + " — " + format.description)
                .collect(Collectors.joining("; "));
    }
}
