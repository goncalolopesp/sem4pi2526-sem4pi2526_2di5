package pt.isep.sem4pi.domain.model.flight;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FlightId implements ValueObject, Comparable<FlightId>, Serializable {

    private String code;

    protected FlightId() {
        // for ORM
    }

    public FlightId(final String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight id cannot be null or empty.");
        }
        this.code = code.trim().toUpperCase();
    }

    public String value() {
        return code;
    }

    @Override
    public int compareTo(final FlightId other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FlightId flightId = (FlightId) o;
        return Objects.equals(code, flightId.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}

