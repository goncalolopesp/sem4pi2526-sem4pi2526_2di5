package pt.isep.sem4pi.domain.route;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Wind implements ValueObject {

    @Column(name = "wind_direction")
    private Integer directionAngle; // 0 to 359 degrees relative to North

    @Column(name = "wind_speed")
    private Double speed; // m/s

    protected Wind() {
        // for ORM
    }

    public Wind(Integer directionAngle, Double speed) {

        Preconditions.nonNull(speed);
        Preconditions.nonNull(directionAngle);

        Preconditions.ensure(directionAngle >= 0 && directionAngle < 360, "Wind direction must be between 0 and 359 degrees.");
        Preconditions.ensure(speed >= 0, "Wind speed cannot be negative.");

        this.directionAngle = directionAngle;
        this.speed = speed;
    }
}