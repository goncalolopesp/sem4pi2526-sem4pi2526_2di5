package pt.isep.sem4pi.domain.model.aircontrolarea;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class AirControlAreaBoundary implements ValueObject, Serializable {

    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;

    protected AirControlAreaBoundary(){
        // for ORM only
    }

    public AirControlAreaBoundary(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        validateBoundaries(minLatitude, maxLatitude, minLongitude, maxLongitude);
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    private void validateBoundaries(double minLat, double maxLat, double minLon, double maxLon) {
        Preconditions.ensure(minLat >= -90 && maxLat <= 90 && minLat < maxLat,
                "Latitude values must be between -90 and 90, and min must be strictly less than max.");
        Preconditions.ensure(minLon >= -180 && maxLon <= 180 && minLon < maxLon,
                "Longitude values must be between -180 and 180, and min must be strictly less than max.");
    }

    public double getMinLatitude(){
        return this.minLatitude;
    }

    public double getMaxLatitude(){
        return this.maxLatitude;
    }

    public double getMinLongitude(){
        return this.minLongitude;
    }

    public double getMaxLongitude(){
        return this.maxLongitude;
    }

    public boolean includes(double latitude, double longitude){
        return (latitude > minLatitude && latitude < maxLatitude) && (longitude > minLongitude && longitude < maxLongitude);
    }

    public AirControlAreaBoundary mergeWith(AirControlAreaBoundary other) {
        double newMinLat = Math.min(this.minLatitude, other.minLatitude);
        double newMaxLat = Math.max(this.maxLatitude, other.maxLatitude);
        double newMinLon = Math.min(this.minLongitude, other.minLongitude);
        double newMaxLon = Math.max(this.maxLongitude, other.maxLongitude);
        return new AirControlAreaBoundary(newMinLat, newMaxLat, newMinLon, newMaxLon);
    }

}
