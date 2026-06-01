package pt.isep.sem4pi.domain.weatherdata;

import jakarta.persistence.Embeddable;

@Embeddable
public class Wind {
    private double speedKnots;
    private double directionDegrees;
    protected Wind() {}
    public Wind(double speedKnots, double directionDegrees) {
        if (speedKnots < 0) throw new IllegalArgumentException("Wind speed cannot be negative.");
        if (directionDegrees < 0 || directionDegrees >= 360) throw new IllegalArgumentException("Wind direction must be 0-359 degrees.");
        this.speedKnots = speedKnots;
        this.directionDegrees = directionDegrees;
    }
    public double getSpeedKnots() { return speedKnots; }
    public double getDirectionDegrees() { return directionDegrees; }
}