package pt.isep.sem4pi.domain.model.route;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;

/**
 * Represents an allowed altitude range for a route segment.
 *
 * An altitude range specifies the minimum and maximum altitude at which an aircraft
 * can fly on a particular segment, along with a slot width that defines the divisions
 * within that range for flight plan allocation.
 *
 * This is a value object that is typically part of a RouteSegment.
 *
 * @author SEM4PI Team
 */
@Embeddable
public class AltitudeRange implements ValueObject {

    /** The minimum altitude in feet */
    @Column(name = "min_altitude", nullable = false)
    private Integer minAltitude;

    /** The maximum altitude in feet */
    @Column(name = "max_altitude", nullable = false)
    private Integer maxAltitude;

    /** The width of the altitude slot in feet */
    @Column(name = "slot_width", nullable = false)
    private Double width; // Width of the altitude slot as per specifications

    /**
     * Default constructor for ORM use only.
     */
    protected AltitudeRange() {
        // for ORM
    }

    /**
     * Creates a new AltitudeRange with the specified minimum, maximum altitudes and width.
     *
     * Validates that:
     * - All parameters are non-null
     * - Minimum altitude is non-negative
     * - Minimum altitude does not exceed maximum altitude
     * - Width is strictly positive
     *
     * @param minAltitude the minimum altitude in feet (must be >= 0)
     * @param maxAltitude the maximum altitude in feet (must be >= minAltitude)
     * @param width the width of the altitude slot in feet (must be > 0)
     *
     * @throws IllegalArgumentException if any parameter is null, minAltitude is negative,
     *                                   minAltitude > maxAltitude, or width is not positive
     */
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

    public Integer minAltitude() {
        return minAltitude;
    }

    public Integer maxAltitude() {
        return maxAltitude;
    }
}