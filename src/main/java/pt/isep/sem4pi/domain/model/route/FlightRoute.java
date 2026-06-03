package pt.isep.sem4pi.domain.model.route;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flight route in the airline system.
 *
 * A flight route is the main aggregation root that defines a path between two airports
 * operated by an airline. Each route consists of one or more segments and can be
 * deactivated on a specified date.
 *
 * The route follows DDD principles where the FlightRouteID is the unique identity and
 * the route manages its segments as part of its aggregate.
 *
 * @author SEM4PI Team
 */
@Entity
@Table(name = "FlightRoute")
public class FlightRoute implements AggregateRoot<FlightRouteID> {

    /** The unique identifier for this flight route */
    @EmbeddedId
    private FlightRouteID routeID;

    /** The IATA code of the airline operating this route */
    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "company_iata_code", nullable = false))
    private IataCodeCompany companyCode;

    /** The IATA code of the origin airport */
    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "origin_airport_iata", nullable = false))
    private IataCodeAirport originAirport;

    /** The IATA code of the destination airport */
    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "destination_airport_iata", nullable = false))
    private IataCodeAirport destinationAirport;

    /** The list of segments that compose this flight route */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "route_id", nullable = false)
    private List<RouteSegment> segments = new ArrayList<>();

    /** The date when this route is deactivated, null if still active */
    @Column
    private LocalDate deactivationDate;

    /** JPA version field for optimistic locking */
    @Version
    private Long version;

    /**
     * Default constructor for ORM use only.
     */
    protected FlightRoute(){
        // for ORM
    }

    /**
     * Creates a new flight route with the specified parameters.
     *
     * Validates that:
     * - All parameters are non-null
     * - The segments list is not empty
     * - The route name starts with the company's IATA code
     * - The route name follows the format: 2 letters + 1-4 numbers
     *
     * @param routeID the unique identifier for this route (2 letters + 1-4 numbers)
     * @param companyCode the IATA code of the company operating this route
     * @param originAirport the IATA code of the origin airport
     * @param destinationAirport the IATA code of the destination airport
     * @param segments the list of segments composing this route
     *
     * @throws IllegalArgumentException if any parameter is null, segments is empty,
     *                                   route name doesn't start with company code,
     *                                   or route name format is invalid
     */
    public FlightRoute(FlightRouteID routeID, IataCodeCompany companyCode, IataCodeAirport originAirport, IataCodeAirport destinationAirport, List<RouteSegment> segments){

        Preconditions.nonNull(routeID, "Route ID cannot be null.");
        Preconditions.nonNull(companyCode, "Company Code cannot be null.");
        Preconditions.nonNull(originAirport, "Origin Airport cannot be null.");
        Preconditions.nonNull(destinationAirport, "Destination Airport cannot be null.");
        Preconditions.nonNull(segments, "Waypoints cannot be null.");

        Preconditions.nonEmpty(segments, "Waypoints cannot be empty.");

        // US073 Validation: Name format and company initials match
        String routeName = routeID.code();
        String companyInitials = companyCode.code();

        if (!routeName.startsWith(companyInitials)) {
            throw new IllegalArgumentException("Route name must start with the company's IATA code initials.");
        }

        if (!routeName.matches("^[A-Za-z]{2}\\d{1,4}$")) {
            throw new IllegalArgumentException("Route name must consist of exactly 2 letters followed by up to 4 numbers.");
        }

        this.routeID = routeID;
        this.companyCode = companyCode;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.segments = new ArrayList<>(segments);
    }

    /**
     * Deactivates this flight route on the specified date.
     *
     * The deactivation date must be today or in the future.
     *
     * @param date the date when this route should be deactivated
     *
     * @throws IllegalArgumentException if date is null or in the past
     */
    public void deactivate(LocalDate date) {
        Preconditions.nonNull(date, "Deactivation date cannot be null.");
        Preconditions.ensure(!date.isBefore(LocalDate.now()), "Deactivation date cannot be in the past.");

        this.deactivationDate = date;
    }

    /**
     * Checks if this route is currently active.
     *
     * A route is active if no deactivation date has been set.
     *
     * @return true if the route is active, false otherwise
     */
    public boolean isActive() {
        return this.deactivationDate == null;
    }

    /**
     * Checks if this route is active on the specified date.
     *
     * A route is active on a given date if it hasn't been deactivated yet,
     * or if the given date is before the deactivation date.
     *
     * @param date the date to check
     * @return true if the route is active on the specified date, false otherwise
     */
    public boolean isActiveOn(LocalDate date) {
        if (this.deactivationDate == null) {
            return true;
        }
        return date.isBefore(this.deactivationDate);
    }

    /**
     * Returns the identity of this flight route.
     *
     * @return the FlightRouteID that uniquely identifies this route
     */
    @Override
    public FlightRouteID identity() {
        return this.routeID;
    }

    /**
     * Checks if this object is the same as another object by comparing identities.
     *
     * Two FlightRoute objects are the same if they have the same identity (FlightRouteID).
     *
     * @param other the object to compare with
     * @return true if both objects represent the same flight route, false otherwise
     */
    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof FlightRoute)) {
            return false;
        }
        final FlightRoute that = (FlightRoute) other;
        if (this == that) {
            return true;
        }
        return identity().equals(that.identity());
    }
}