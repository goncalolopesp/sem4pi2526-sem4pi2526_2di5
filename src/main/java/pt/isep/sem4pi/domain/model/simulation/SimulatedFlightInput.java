package pt.isep.sem4pi.domain.model.simulation;

import java.util.Objects;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SimulatedFlightInput {

    @Column(nullable = false)
    private String flightPlanId;

    protected SimulatedFlightInput() {
        // for ORM
    }

    public SimulatedFlightInput(final String flightPlanId) {
        Preconditions.nonEmpty(flightPlanId, "Flight plan id is required");

        this.flightPlanId = flightPlanId.trim();
    }

    public String flightPlanId() {
        return flightPlanId;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SimulatedFlightInput)) {
            return false;
        }

        final SimulatedFlightInput that = (SimulatedFlightInput) other;
        return this.flightPlanId.equals(that.flightPlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightPlanId);
    }
}
