package pt.isep.sem4pi.domain.model.simulation;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.Objects;

@Embeddable
public class FlightExecutionSummary {

    // DEFESA:
    // Este Value Object descreve o resultado de um voo por valor e pertence ao agregado SimulationReport.
    @Column(name = "flight_id", nullable = false)
    private String flightId;

    @Column(name = "execution_status", nullable = false)
    private String executionStatus;

    protected FlightExecutionSummary() {
        // for ORM
    }

    public FlightExecutionSummary(final String flightId, final String executionStatus) {
        Preconditions.nonEmpty(flightId, "Flight id is required");
        Preconditions.nonEmpty(executionStatus, "Execution status is required");
        this.flightId = flightId.trim();
        this.executionStatus = executionStatus.trim().toUpperCase(Locale.ROOT);
    }

    public String flightId() {
        return flightId;
    }

    public String executionStatus() {
        return executionStatus;
    }

    public boolean completedSuccessfully() {
        return "FINISHED".equals(executionStatus);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof FlightExecutionSummary)) {
            return false;
        }
        final FlightExecutionSummary that = (FlightExecutionSummary) other;
        return flightId.equals(that.flightId)
                && executionStatus.equals(that.executionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, executionStatus);
    }
}
