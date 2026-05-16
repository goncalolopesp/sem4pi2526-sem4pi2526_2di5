package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Coefficient is a Value Object that encapsulates the aerodynamic coefficients of an aircraft model, specifically the drag coefficient and lift coefficient.
 * It includes validation to ensure that both coefficients are positive, non-null, and within realistic bounds.
 * This class is designed to be embedded within the AircraftModel entity, allowing for a clean separation of concerns.
 */

@Embeddable
public class Coefficient implements ValueObject {

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal dragCoefficient;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal liftCoefficient;

    protected Coefficient() {
        // for ORM
    }

    public Coefficient(BigDecimal dragCoefficient, BigDecimal liftCoefficient) {
        validateDrag(dragCoefficient);
        validateLift(liftCoefficient);

        this.dragCoefficient = dragCoefficient;
        this.liftCoefficient = liftCoefficient;
    }

    private void validateDrag(BigDecimal cd) {
        Preconditions.nonNull(cd, "Drag Coefficient cannot be null.");

        if (cd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Drag Coefficient must be strictly greater than zero.");
        }

        if (cd.compareTo(new BigDecimal("1.0")) >= 0) {
            throw new IllegalArgumentException("Drag Coefficient is unrealistically high (must be <= 1.0).");
        }
    }

    private void validateLift(BigDecimal cl) {
        Preconditions.nonNull(cl, "Lift Coefficient cannot be null.");

        if (cl.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Lift Coefficient must be strictly greater than zero.");
        }

        if (cl.compareTo(new BigDecimal("4.0")) >= 0) {
            throw new IllegalArgumentException("Lift Coefficient is unrealistically high (must be <= 4.0).");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coefficient that = (Coefficient) o;
        return this.dragCoefficient.compareTo(that.dragCoefficient) == 0 &&
                this.liftCoefficient.compareTo(that.liftCoefficient) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dragCoefficient.doubleValue(), liftCoefficient.doubleValue());
    }

    @Override
    public String toString() {
        return "Drag: " + this.dragCoefficient + ", Lift: " + this.liftCoefficient;
    }
}