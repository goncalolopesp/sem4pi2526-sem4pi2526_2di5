package pt.isep.sem4pi.domain.route;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class FlightRouteID implements ValueObject, Comparable<FlightRouteID> {

    private String routeID;

    protected FlightRouteID(){
        // for ORM
    }

    public FlightRouteID(String routeID){
        if (routeID == null || routeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty.");
        }
        if (!routeID.matches("^[A-Za-z]{2}\\d{1,4}$")) {
            throw new IllegalArgumentException("Route ID must consist of exactly 2 letters followed by up to 4 numbers (e.g., TP123).");
        }
        this.routeID = routeID.toUpperCase();
    }

    public String code() {
        return routeID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightRouteID)) return false;
        FlightRouteID that = (FlightRouteID) o;
        return routeID.equals(that.routeID);
    }

    @Override
    public int hashCode() {
        return routeID.hashCode();
    }

    @Override
    public int compareTo(FlightRouteID other) {
        return this.routeID.compareTo(other.routeID);
    }

    @Override
    public String toString() {
        return routeID;
    }
}