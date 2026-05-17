package pt.isep.sem4pi.domain.model.weatherdata;

import jakarta.persistence.Embeddable;

@Embeddable
public class AtmosphericPressure {
    private double hPa;
    protected AtmosphericPressure() {}
    public AtmosphericPressure(double hPa) {
        if (hPa < 800.0 || hPa > 1150.0) throw new IllegalArgumentException("Pressure must be between 800 and 1150 hPa.");
        this.hPa = hPa;
    }
    public double getHPa() { return hPa; }
}