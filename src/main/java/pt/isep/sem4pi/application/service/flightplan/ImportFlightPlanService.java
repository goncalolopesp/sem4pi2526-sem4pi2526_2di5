package pt.isep.sem4pi.application.service.flightplan;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.us081.US081FlightPlanMapper;
import pt.isep.sem4pi.us120.US120FlightPlanValidator;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@ApplicationService
public class ImportFlightPlanService {

    private final CreateFlightPlanService createFlightPlanService;

    public ImportFlightPlanService(final CreateFlightPlanService createFlightPlanService) {
        this.createFlightPlanService = createFlightPlanService;
    }

    public FlightPlanImportResultDTO importFlightPlan(final String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path is required.");
        }

        final Path path = Path.of(filePath.trim());
        final US120ValidationResult validation;
        try {
            validation = US120FlightPlanValidator.analyze(path);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to read file: " + e.getMessage());
        }

        if (!validation.isValid()) {
            return FlightPlanImportResultDTO.failure(validation.getAllErrors());
        }

        final CreateFlightPlanDTO dto = US081FlightPlanMapper.map(validation.getParseTree());
        try {
            final FlightPlan flightPlan = createFlightPlanService.createFlightPlanFromDsl(dto, path);
            return FlightPlanImportResultDTO.success(flightPlan.identity().value());
        } catch (final IllegalArgumentException e) {
            return FlightPlanImportResultDTO.failure(List.of(e.getMessage()));
        }
    }
}
