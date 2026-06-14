package pt.isep.sem4pi.domain.model.flight.DTO;

import pt.isep.sem4pi.us120.US120ValidationResult;

public class TestFlightPlanResultDTO {

    private final String flightPlanId;
    private final US120ValidationResult dslValidation;
    private final FlightTestExecutionResultDTO flightTest;
    private final boolean accepted;
    private final String message;

    public TestFlightPlanResultDTO(final String flightPlanId,
                                   final US120ValidationResult dslValidation,
                                   final FlightTestExecutionResultDTO flightTest,
                                   final boolean accepted,
                                   final String message) {
        this.flightPlanId = flightPlanId;
        this.dslValidation = dslValidation;
        this.flightTest = flightTest;
        this.accepted = accepted;
        this.message = message;
    }

    public String flightPlanId() {
        return flightPlanId;
    }

    public US120ValidationResult dslValidation() {
        return dslValidation;
    }

    public FlightTestExecutionResultDTO flightTest() {
        return flightTest;
    }

    public boolean accepted() {
        return accepted;
    }

    public String message() {
        return message;
    }
}
