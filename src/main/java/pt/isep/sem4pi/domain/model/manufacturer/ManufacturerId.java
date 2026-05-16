package pt.isep.sem4pi.domain.model.manufacturer;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
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
        Preconditions.ensure(code != null && !code.trim().isEmpty(),
                "The Manufacturer code cannot be null or empty!");
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public int compareTo(ManufacturerId o) {
        return this.code.compareTo(o.getCode());
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