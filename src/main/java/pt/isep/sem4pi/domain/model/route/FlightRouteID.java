package pt.isep.sem4pi.domain.model.route;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

/**
 * Represents a unique identifier for a flight route.
 *
 * A FlightRouteID is a value object that encapsulates the unique code for a flight route.
 * The route ID follows the format: 2 uppercase letters followed by 1 to 4 digits.
 * For example: "TP12", "AF001", "BA1234".
 *
 * This class is immutable and can be compared with other FlightRouteID objects.
 *
 * @author SEM4PI Team
 */
@Embeddable
public class FlightRouteID implements ValueObject, Comparable<FlightRouteID> {

    /** The route identifier code */
    private String routeID;

    /**
     * Default constructor for ORM use only.
     */
    protected FlightRouteID(){
        // for ORM
    }

    /**
     * Creates a new FlightRouteID with the specified code.
     *
     * The route ID must follow the format: 2 letters + 1-4 digits.
     * The letters are automatically converted to uppercase.
     *
     * @param routeID the route identifier code (e.g., "TP12", "BA001")
     *
     * @throws IllegalArgumentException if routeID is null, empty, blank,
     *                                   or doesn't match the required format
     */
    public FlightRouteID(String routeID){
        if (routeID == null || routeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty.");
        }
        if (!routeID.matches("^[A-Za-z]{2}\\d{1,4}$")) {
            throw new IllegalArgumentException("Route ID must consist of exactly 2 letters followed by up to 4 numbers (e.g., TP123).");
        }
        this.routeID = routeID.toUpperCase();
    }

    /**
     * Returns the route identifier code.
     *
     * @return the uppercase route ID code
     */
    public String code() {
        return routeID;
    }

    /**
     * Checks equality based on the route ID code.
     *
     * @param o the object to compare with
     * @return true if both objects are FlightRouteID instances with the same code
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightRouteID)) return false;
        FlightRouteID that = (FlightRouteID) o;
        return routeID.equals(that.routeID);
    }

    /**
     * Returns the hash code based on the route ID code.
     *
     * @return the hash code of the route ID
     */
    @Override
    public int hashCode() {
        return routeID.hashCode();
    }

    /**
     * Compares this FlightRouteID with another based on their route ID codes.
     *
     * @param other the other FlightRouteID to compare with
     * @return 0 if equal, negative if this is less than other, positive if greater
     */
    @Override
    public int compareTo(FlightRouteID other) {
        return this.routeID.compareTo(other.routeID);
    }

    /**
     * Returns the string representation of this FlightRouteID.
     *
     * @return the route ID code
     */
    @Override
    public String toString() {
        return routeID;
    }
}