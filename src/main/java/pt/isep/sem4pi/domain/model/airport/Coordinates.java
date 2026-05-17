package pt.isep.sem4pi.domain.model.airport;

import jakarta.persistence.Embeddable;

@Embeddable
public class Coordinates {
    private double latitude;
    private double longitude;

    protected Coordinates() {}

    public Coordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}