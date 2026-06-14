package pt.isep.sem4pi.domain.model.flight.DTO;

import java.util.Collections;
import java.util.List;

public class FlightPlanImportResultDTO {

    private final boolean success;
    private final String flightPlanId;
    private final List<String> errors;

    private FlightPlanImportResultDTO(final boolean success,
                                      final String flightPlanId,
                                      final List<String> errors) {
        this.success = success;
        this.flightPlanId = flightPlanId;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static FlightPlanImportResultDTO success(final String flightPlanId) {
        return new FlightPlanImportResultDTO(true, flightPlanId, List.of());
    }

    public static FlightPlanImportResultDTO failure(final List<String> errors) {
        return new FlightPlanImportResultDTO(false, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFlightPlanId() {
        return flightPlanId;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
