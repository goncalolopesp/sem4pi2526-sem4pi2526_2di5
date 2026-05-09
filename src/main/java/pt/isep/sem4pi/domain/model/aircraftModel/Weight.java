package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing the weight characteristics of an aircraft. *
 * <p>This class encapsulates three critical weight parameters used in aircraft * performance calculations and operational planning:</p> * <ul> *   <li><b>Empty Weight (EW)</b>: The weight of the aircraft without fuel, cargo, or passengers</li> *   <li><b>Maximum Zero Fuel Weight (MZFW)</b>: Maximum weight for safe operation without fuel</li> *   <li><b>Maximum Takeoff Weight (MTOW)</b>: Maximum permissible weight at takeoff</li> * </ul> *
 * <p>The weights must maintain the following relationship: EW < MZFW < MTOW</p> *
 * <p>This class is embeddable for use in JPA/Hibernate persistence.</p>
 */

@Embeddable

public class Weight implements ValueObject {

    /**
     * The aircraft weight without fuel, cargo, or payload, in kilograms
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal emptyWeight;

    /**
     * The maximum weight allowed without fuel, in kilograms
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxZeroFuelWeight;

    /**
     * The maximum weight allowed at takeoff, in kilograms
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxTakeOffWeight;

    /**
     * Protected no-argument constructor required by the ORM framework.
     * <p>This constructor is not intended for direct use.</p>
     */
    protected Weight() {
    }

    /**
     * Constructs a Weight value object with the specified weight parameters.     *
     *
     * @param emptyWeight the aircraft's empty weight (must be strictly greater than zero)     * @param maxZeroFuelWeight the maximum zero fuel weight (must be > empty weight)     * @param maxTakeOffWeight the maximum takeoff weight (must be > MZFW)     *
     * @throws IllegalArgumentException if any of the weights are null, non-positive, or if the weight relationships are violated
     */

    public Weight(BigDecimal emptyWeight, BigDecimal maxZeroFuelWeight, BigDecimal maxTakeOffWeight) {
        validateEmptyWeight(emptyWeight);
        validateMzfw(maxZeroFuelWeight);
        validateMtow(maxTakeOffWeight);

        if (maxZeroFuelWeight.compareTo(emptyWeight) <= 0) {
            throw new IllegalArgumentException("MZFW must be strictly greater than Empty Weight.");
        }
        if (maxTakeOffWeight.compareTo(maxZeroFuelWeight) <= 0) {
            throw new IllegalArgumentException("MTOW must be strictly greater than MZFW.");
        }

        this.emptyWeight = emptyWeight;
        this.maxZeroFuelWeight = maxZeroFuelWeight;
        this.maxTakeOffWeight = maxTakeOffWeight;
    }

    /**
     * Validates that the maximum zero fuel weight is valid.     *
     *
     * @param mzfw the weight value to validate
     * @throws IllegalArgumentException if the weight is null or non-positive
     */
    private void validateMzfw(BigDecimal mzfw) {
        if (mzfw == null) throw new IllegalArgumentException("Max zero fuel weight cannot be null.");
        if (mzfw.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Max zero fuel weight must be strictly greater than zero.");
    }

    /**
     * Validates that the empty weight is valid.     *
     *
     * @param ew the weight value to validate     *
     * @throws IllegalArgumentException if the weight is null or non-positive
     */
    private void validateEmptyWeight(BigDecimal ew) {
        if (ew == null) throw new IllegalArgumentException("Empty weight cannot be null.");
        if (ew.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Empty weight must be strictly greater than zero.");
    }

    /**
     * Validates that the maximum takeoff weight is valid.     *
     *
     * @param mtow the weight value to validate
     * @throws IllegalArgumentException if the weight is null or non-positive
     */
    private void validateMtow(BigDecimal mtow) {
        if (mtow == null) throw new IllegalArgumentException("Max take off weight cannot be null.");
        if (mtow.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Max take off weight must be strictly greater than zero.");
    }

    /**
     * Returns the empty weight of the aircraft.
     *
     * @return the empty weight in kilograms
     */
    public BigDecimal emptyWeight() {
        return this.emptyWeight;
    }

    /**
     * Returns the maximum zero fuel weight of the aircraft.
     *
     * @return the maximum zero fuel weight in kilograms
     */
    public BigDecimal maximumZeroFuelWeight() {
        return this.maxZeroFuelWeight;
    }

    /**
     * Returns the maximum takeoff weight of the aircraft.
     *
     * @return the maximum takeoff weight in kilograms
     */
    public BigDecimal maximumTakeOffWeight() {
        return this.maxTakeOffWeight;
    }

    /**
     * Compares this Weight object with another object for equality.
     * Two Weight objects are equal if their empty weight, maximum zero fuel weight,
     * and maximum takeoff weight are numerically equal.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    // Structural equality comparing values instead of object scale
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weight weight = (Weight) o;
        return this.emptyWeight.compareTo(weight.emptyWeight()) == 0 &&
                this.maxZeroFuelWeight.compareTo(weight.maximumZeroFuelWeight()) == 0 &&
                this.maxTakeOffWeight.compareTo(weight.maximumTakeOffWeight()) == 0;
    }

    /**
     * Returns the hash code for this Weight object.
     * The hash code is based on the double values of the weight fields.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.emptyWeight.doubleValue(), this.maxZeroFuelWeight.doubleValue(), this.maxTakeOffWeight.doubleValue());
    }
}