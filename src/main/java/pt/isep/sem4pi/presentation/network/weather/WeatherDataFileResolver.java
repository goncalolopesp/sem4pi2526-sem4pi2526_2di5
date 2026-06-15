package pt.isep.sem4pi.presentation.network.weather;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class WeatherDataFileResolver {

    private static final String[] EXTENSIONS = {"xlsx", "csv"};
    private static final Path DATA_DIR = Path.of(
            "src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData");

    private WeatherDataFileResolver() {
        // utility
    }

    public static Optional<ResolvedWeatherFile> resolve(final String userInput) throws IOException {
        if (userInput == null || userInput.trim().isEmpty()) {
            return Optional.empty();
        }
        final String trimmed = userInput.trim();
        final Path direct = Path.of(trimmed);
        if (Files.isRegularFile(direct)) {
            return Optional.of(new ResolvedWeatherFile(direct, detectType(direct)));
        }

        if (!hasExtension(trimmed)) {
            for (final String extension : EXTENSIONS) {
                final Path withExtension = Path.of(trimmed + "." + extension);
                if (Files.isRegularFile(withExtension)) {
                    return Optional.of(new ResolvedWeatherFile(withExtension, extension));
                }
                final Path inDataDir = DATA_DIR.resolve(trimmed + "." + extension);
                if (Files.isRegularFile(inDataDir)) {
                    return Optional.of(new ResolvedWeatherFile(inDataDir, extension));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean hasExtension(final String path) {
        final String name = Path.of(path).getFileName().toString();
        final int dot = name.lastIndexOf('.');
        return dot > 0 && dot < name.length() - 1;
    }

    private static String detectType(final Path path) {
        final String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".xlsx")) {
            return "xlsx";
        }
        if (name.endsWith(".csv")) {
            return "csv";
        }
        return "";
    }

    public record ResolvedWeatherFile(Path path, String fileType) {
    }
}
