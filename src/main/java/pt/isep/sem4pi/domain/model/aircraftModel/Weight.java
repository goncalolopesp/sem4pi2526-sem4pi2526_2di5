package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Weight implements ValueObject {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal emptyWeight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxZeroFuelWeight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxTakeOffWeight;

    protected Weight() {
        // Required by ORM
    }

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

    // Validates if the specific weight is null or non-positive
    private void validateMzfw(BigDecimal mzfw) {
        if (mzfw == null) throw new IllegalArgumentException("Max zero fuel weight cannot be null.");
        if (mzfw.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Max zero fuel weight must be strictly greater than zero.");
    }

    private void validateEmptyWeight(BigDecimal ew) {
        if (ew == null) throw new IllegalArgumentException("Empty weight cannot be null.");
        if (ew.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Empty weight must be strictly greater than zero.");
    }

    private void validateMtow(BigDecimal mtow) {
        if (mtow == null) throw new IllegalArgumentException("Max take off weight cannot be null.");
        if (mtow.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Max take off weight must be strictly greater than zero.");
    }

    public BigDecimal emptyWeight() {
        return this.emptyWeight;
    }

    public BigDecimal maximumZeroFuelWeight() {
        return this.maxZeroFuelWeight;
    }

    public BigDecimal maximumTakeOffWeight() {
        return this.maxTakeOffWeight;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(this.emptyWeight.doubleValue(), this.maxZeroFuelWeight.doubleValue(), this.maxTakeOffWeight.doubleValue());
    }
}