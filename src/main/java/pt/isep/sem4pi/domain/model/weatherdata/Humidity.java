package pt.isep.sem4pi.domain.model.weatherdata;

import jakarta.persistence.Embeddable;

@Embeddable
public class Humidity {
    private double percentage;
    protected Humidity() {}
    public Humidity(double percentage) {
        if (percentage < 0.0 || percentage > 100.0) throw new IllegalArgumentException("Humidity must be between 0 and 100.");
        this.percentage = percentage;
    }
    public double getPercentage() { return percentage; }
}