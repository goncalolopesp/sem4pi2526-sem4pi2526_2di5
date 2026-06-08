package pt.isep.sem4pi.domain.model.route;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Represents wind conditions for a route segment.
 * 
 * Wind is a value object that encapsulates wind direction and speed information
 * used in flight route segments. The direction is specified in degrees relative to North
 * (0-359), and the speed is specified in meters per second.
 * 
 * @author SEM4PI Team
 */
@Embeddable
public class Wind implements ValueObject {

    /** The wind direction in degrees relative to North (0-359) */
    @Column(name = "wind_direction")
    private Integer directionAngle; // 0 to 359 degrees relative to North

    /** The wind speed in meters per second */
    @Column(name = "wind_speed")
    private Double speed; // m/s

    /**
     * Default constructor for ORM use only.
     */
    protected Wind() {
        // for ORM
    }

    /**
     * Creates a new Wind object with the specified direction and speed.
     * 
     * The direction must be between 0 and 359 degrees (inclusive).
     * The speed must be non-negative.
     * 
     * @param directionAngle the wind direction in degrees relative to North (0-359)
     * @param speed the wind speed in meters per second (must be >= 0)
     * 
     * @throws IllegalArgumentException if directionAngle is outside 0-359 range
     *                                   or if speed is negative, or if any parameter is null
     */
    public Wind(Integer directionAngle, Double speed) {

        Preconditions.nonNull(speed);
        Preconditions.nonNull(directionAngle);

        Preconditions.ensure(directionAngle >= 0 && directionAngle < 360, "Wind direction must be between 0 and 359 degrees.");
        Preconditions.ensure(speed >= 0, "Wind speed cannot be negative.");

        this.directionAngle = directionAngle;
        this.speed = speed;
    }
}