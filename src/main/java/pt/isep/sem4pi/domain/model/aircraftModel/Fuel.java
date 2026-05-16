package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Fuel implements ValueObject {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxFuelCapacity;

    protected Fuel() {
        // for ORM
    }

    public Fuel(BigDecimal maxFuelCapacity) {
        validateMfc(maxFuelCapacity);

        this.maxFuelCapacity = maxFuelCapacity;
    }

    private void validateMfc(BigDecimal cd) {
       Preconditions.nonNull(cd, "Max fuel capacity cannot be null.");

        if (cd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max fuel capacity must be strictly greater than zero.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fuel that = (Fuel) o;
        return this.maxFuelCapacity.compareTo(that.maxFuelCapacity) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFuelCapacity.doubleValue());
    }

    @Override
    public String toString() {
        return "Fuel{" +
                "maxFuelCapacity=" + maxFuelCapacity +
                '}';
    }
}