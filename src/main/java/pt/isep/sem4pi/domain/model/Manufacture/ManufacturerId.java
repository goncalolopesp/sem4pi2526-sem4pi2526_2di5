package pt.isep.sem4pi.domain.model.Manufacture;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ManufacturerId implements ValueObject, Comparable<ManufacturerId>, Serializable {

    private String code;

    protected ManufacturerId() {
        // for ORM
    }

    public ManufacturerId(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("The Manufacturer code cannot be null or empty!");
        }
        this.code = code;
    }

    @Override
    public int compareTo(ManufacturerId o) {
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManufacturerId that = (ManufacturerId) o;
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