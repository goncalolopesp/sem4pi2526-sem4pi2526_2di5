package pt.isep.sem4pi.domain.model.aircraft;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AircraftRegistration implements ValueObject, Comparable<AircraftRegistration>, Serializable {

    private String code;

    protected AircraftRegistration() {
        // for ORM
    }

    public AircraftRegistration(final String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Aircraft registration cannot be null or empty.");
        }
        this.code = code.trim().toUpperCase();
    }

    public String value() {
        return code;
    }

    @Override
    public int compareTo(final AircraftRegistration other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AircraftRegistration that = (AircraftRegistration) o;
        return Objects.equals(code, that.code);
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

