package pt.isep.sem4pi.domain.weatherdata;

import jakarta.persistence.Embeddable;

@Embeddable
public class Altitude {
    private double meters;
    protected Altitude() {}
    public Altitude(double meters) {
        if (meters < -500.0 || meters > 50000.0) throw new IllegalArgumentException("Altitude must be a valid atmospheric height.");
        this.meters = meters;
    }
    public double getMeters() { return meters; }
}