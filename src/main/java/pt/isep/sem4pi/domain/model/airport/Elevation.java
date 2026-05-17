package pt.isep.sem4pi.domain.model.airport;

import jakarta.persistence.Embeddable;

@Embeddable
public class Elevation {
    private double metersAboveSeaLevel;

    protected Elevation() {}

    public Elevation(double meters) {
        if (meters < -500 || meters > 10000) {
            throw new IllegalArgumentException("Elevation must be a valid Earth altitude (between -500m and 10000m).");
        }
        this.metersAboveSeaLevel = meters;
    }

    public double getMeters() { return metersAboveSeaLevel; }
}