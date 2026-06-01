package pt.isep.sem4pi.domain.route;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;

@Embeddable
public class AltitudeRange implements ValueObject {

    @Column(name = "min_altitude", nullable = false)
    private Integer minAltitude;

    @Column(name = "max_altitude", nullable = false)
    private Integer maxAltitude;

    @Column(name = "slot_width", nullable = false)
    private Double width; // Width of the altitude slot as per specifications

    protected AltitudeRange() {
        // for ORM
    }

    public AltitudeRange(Integer minAltitude, Integer maxAltitude, Double width) {

        Preconditions.nonNull(minAltitude, "minAltitude cannot be null.");
        Preconditions.nonNull(maxAltitude, "maxAltitude cannot be null.");
        Preconditions.nonNull(width, "width cannot be null.");

        Preconditions.ensure(minAltitude >= 0, "Minimum altitude cannot be negative.");
        Preconditions.ensure(minAltitude <= maxAltitude, "Minimum altitude cannot be greater than maximum altitude.");
        Preconditions.ensure(width > 0, "Width must be strictly positive.");

        this.minAltitude = minAltitude;
        this.maxAltitude = maxAltitude;
        this.width = width;
    }
}