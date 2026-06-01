package pt.isep.sem4pi.domain.weatherdata;

import jakarta.persistence.Embeddable;

@Embeddable
public class Temperature {
    private double celsius;
    protected Temperature() {}
    public Temperature(double celsius) {
        if (celsius < -90.0 || celsius > 60.0) throw new IllegalArgumentException("Temperature must be between -90C and +60C.");
        this.celsius = celsius;
    }
    public double getCelsius() { return celsius; }
}