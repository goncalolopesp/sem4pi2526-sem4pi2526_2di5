package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing the wing geometry of an aircraft.
 * <p>This class encapsulates the wing area and aspect ratio, which are essential
 * for aerodynamic calculations and aircraft performance modeling.</p>
 * <p>This class is embeddable for use in JPA/Hibernate persistence.</p>
 */
@Embeddable
public class WingGeometry implements ValueObject {

    /**
     * The area of the wing in square meters.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal wingArea;

    /**
     * The aspect ratio of the wing, defined as the square of the wingspan divided by the wing area.
     */
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal aspectRatio;

    /**
     * Protected no-argument constructor required by the ORM framework.
     * <p>This constructor is not intended for direct use.</p>
     */
    protected WingGeometry() {}

    /**
     * Constructs a WingGeometry value object with the specified wing area and aspect ratio.
     *
     * @param wingArea the area of the wing (must be strictly greater than zero)
     * @param aspectRatio the aspect ratio of the wing (must be strictly greater than zero)
     * @throws IllegalArgumentException if wingArea or aspectRatio is null or non-positive
     */
    public WingGeometry(BigDecimal wingArea, BigDecimal aspectRatio) {
        validateWingArea(wingArea);
        validateAspectRatio(aspectRatio);

        this.wingArea = wingArea;
        this.aspectRatio = aspectRatio;
    }

    /**
     * Validates that the wing area is valid.
     *
     * @param area the wing area value to validate
     * @throws IllegalArgumentException if the area is null or non-positive
     */
    private void validateWingArea(BigDecimal area) {
        if (area == null || area.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Wing Area must be strictly greater than zero.");
        }
    }

    /**
     * Validates that the aspect ratio is valid.
     *
     * @param ar the aspect ratio value to validate
     * @throws IllegalArgumentException if the aspect ratio is null or non-positive
     */
    private void validateAspectRatio(BigDecimal ar) {
        if (ar == null || ar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Aspect Ratio must be strictly greater than zero.");
        }
    }

    /**
     * Returns the wing area.
     *
     * @return the wing area in square meters
     */
    public BigDecimal wingArea() {return this.wingArea;}

    /**
     * Returns the aspect ratio.
     *
     * @return the aspect ratio
     */
    public BigDecimal aspectRatio() {return this.aspectRatio;}

    /**
     * Compares this WingGeometry object with another object for equality.
     * Two WingGeometry objects are equal if their wing area and aspect ratio are numerically equal.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    // Structural equality comparing values instead of object scale
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WingGeometry wingGeometry = (WingGeometry) o;
        return this.aspectRatio.compareTo(wingGeometry.aspectRatio()) == 0 &&
                this.wingArea.compareTo(wingGeometry.wingArea()) == 0;
    }

    /**
     * Returns the hash code for this WingGeometry object.
     * The hash code is based on the double values of the wing area and aspect ratio.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.aspectRatio.doubleValue(), this.wingArea.doubleValue());
    }
}