package pt.isep.sem4pi.application.service.flightplan;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;
import pt.isep.sem4pi.us120.US120FlightPlanValidator;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * US121 — create a valid flight plan from a file.
 * Validates file format and US120 rules before importing into the domain.
 */
@ApplicationService
public class CreateFlightPlanFromFileService {

    private final ImportFlightPlanService importFlightPlanService;

    public CreateFlightPlanFromFileService(final ImportFlightPlanService importFlightPlanService) {
        this.importFlightPlanService = importFlightPlanService;
    }

    public FlightPlanImportResultDTO validateFile(final String filePath) {
        final Path path = resolveExistingPath(filePath);
        final FlightPlanImportResultDTO formatResult = validateSupportedFormat(path);
        if (formatResult != null) {
            return formatResult;
        }
        return toValidationResult(analyzeWithUs120(path));
    }

    public FlightPlanImportResultDTO createFromFile(final String filePath) {
        final FlightPlanImportResultDTO validation = validateFile(filePath);
        if (!validation.isSuccess()) {
            return validation;
        }
        return importFlightPlanService.importFlightPlan(filePath.trim());
    }

    private static FlightPlanImportResultDTO validateSupportedFormat(final Path path) {
        if (FlightPlanFileFormat.fromPath(path).isPresent()) {
            return null;
        }
        final String fileName = path.getFileName().toString();
        return FlightPlanImportResultDTO.failure(List.of(
                "Unsupported file format for '" + fileName + "'. "
                        + "Supported formats: " + FlightPlanFileFormat.supportedFormatsMessage()));
    }

    private static US120ValidationResult analyzeWithUs120(final Path path) {
        try {
            return US120FlightPlanValidator.analyze(path);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to read file: " + e.getMessage());
        }
    }

    private static FlightPlanImportResultDTO toValidationResult(final US120ValidationResult validation) {
        if (validation.isValid()) {
            return FlightPlanImportResultDTO.success(null);
        }
        return FlightPlanImportResultDTO.failure(validation.getAllErrors());
    }

    private static Path resolveExistingPath(final String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path is required.");
        }
        final Path path = Path.of(filePath.trim());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File doesn't exist: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path is not a regular file: " + path);
        }
        return path;
    }
}
