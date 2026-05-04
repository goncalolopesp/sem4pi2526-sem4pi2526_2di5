package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class WingGeometry implements ValueObject {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal wingArea;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal aspectRatio;

    protected WingGeometry() {}

    public WingGeometry(BigDecimal wingArea, BigDecimal aspectRatio) {
        validateWingArea(wingArea);
        validateAspectRatio(aspectRatio);

        this.wingArea = wingArea;
        this.aspectRatio = aspectRatio;
    }

    private void validateWingArea(BigDecimal area) {
        if (area == null || area.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Wing Area must be strictly greater than zero.");
        }
    }

    private void validateAspectRatio(BigDecimal ar) {
        if (ar == null || ar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Aspect Ratio must be strictly greater than zero.");
        }
    }

    public BigDecimal wingArea() {return this.wingArea;}
    public BigDecimal aspectRatio() {return this.aspectRatio;}

    // Structural equality comparing values instead of object scale
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WingGeometry wingGeometry = (WingGeometry) o;
        return this.aspectRatio.compareTo(wingGeometry.aspectRatio()) == 0 &&
                this.wingArea.compareTo(wingGeometry.wingArea()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.aspectRatio.doubleValue(), this.wingArea.doubleValue());
    }
}