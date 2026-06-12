package pt.isep.sem4pi.domain.model.simulation;

public enum SimulationValidationResult {
    PASS,
    FAIL;

    public static SimulationValidationResult from(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Simulation validation result is required");
        }
        return valueOf(value.trim().toUpperCase());
    }
}
