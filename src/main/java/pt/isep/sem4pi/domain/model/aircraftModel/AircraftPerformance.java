package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Aircraft Performance is a Value Object that encapsulates the performance characteristics of an aircraft model, including service ceiling, cruise speed, and maximum range. It includes validation to ensure that all values are positive and non-null.
 * This class is designed to be embedded within the AircraftModel entity, allowing for a clean separation of concerns.
 */

@Embeddable
public class AircraftPerformance implements ValueObject {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceCeiling;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cruiseSpeed;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumRangeKm;

    protected AircraftPerformance() {
        // for ORM
    }

    public AircraftPerformance(BigDecimal serviceCeiling,  BigDecimal cruiseSpeed, BigDecimal maximumRange) {
        validateServiceCeiling(serviceCeiling);
        validateCruiseSpeed(cruiseSpeed);
        validateMaxRange(maximumRange);

        this.serviceCeiling = serviceCeiling;
        this.cruiseSpeed = cruiseSpeed;
        this.maximumRangeKm = maximumRange;
    }

    private void validateServiceCeiling(BigDecimal cd) {
        if (cd == null) {
            throw new IllegalArgumentException("Service Ceiling cannot be null.");
        }

        if (cd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Service Ceiling must be strictly greater than zero.");
        }
    }

    private void validateCruiseSpeed(BigDecimal cd) {
        if (cd == null) {
            throw new IllegalArgumentException("Cruise Speed cannot be null.");
        }

        if (cd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cruise Speed must be strictly greater than zero.");
        }
    }

    private void validateMaxRange(BigDecimal cd) {
        if (cd == null) {
            throw new IllegalArgumentException("Max Range cannot be null.");
        }

        if (cd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max Range must be strictly greater than zero.");
        }
    }

    public BigDecimal serviceCeiling() {return serviceCeiling;}
    public BigDecimal cruiseSpeed() {return cruiseSpeed;}
    public BigDecimal maximumRangeKm() {return maximumRangeKm;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftPerformance that = (AircraftPerformance) o;
        return this.serviceCeiling.compareTo(that.serviceCeiling) == 0 &&
                this.cruiseSpeed.compareTo(that.cruiseSpeed) == 0 &&
                this.maximumRangeKm.compareTo(that.maximumRangeKm) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serviceCeiling.doubleValue(), this.cruiseSpeed.doubleValue(), this.maximumRangeKm.doubleValue());
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "serviceCeiling: %.2f, cruiseSpeed: %.2f, maximumRange: %.2f",
                this.serviceCeiling, this.cruiseSpeed, this.maximumRangeKm);
    }
}