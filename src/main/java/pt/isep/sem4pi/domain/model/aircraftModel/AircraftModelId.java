package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AircraftModelId implements ValueObject, Comparable<AircraftModelId>, Serializable {

    private String code;

    protected AircraftModelId() {
        // for ORM
    }

    public AircraftModelId(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("The Aircraft Model code cannot be null or empty!");
        }
        this.code = code;
    }

    @Override
    public int compareTo(AircraftModelId o) {
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftModelId that = (AircraftModelId) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return this.code;
    }
}