package pt.isep.sem4pi.domain.model.route;

import eapli.framework.domain.model.DomainEntity;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.airport.Coordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a segment of a flight route.
 * 
 * A route segment is a part of a complete flight route, connecting two waypoints
 * with specific geographical coordinates, wind conditions, altitude restrictions,
 * and maximum speed limits. A flight route typically consists of one or more segments.
 * 
 * @author SEM4PI Team
 */
@Entity
@Table(name = "RouteSegment")
public class RouteSegment implements DomainEntity<Long> {

    /** The unique identifier for this segment */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long segmentId;

    /** The starting point coordinates of this segment */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_lon"))
    })
    private Coordinates startNodeCoords;

    /** The ending point coordinates of this segment */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_lon"))
    })
    private Coordinates endNodeCoords;

    /** The wind conditions for this segment */
    @Embedded
    private Wind wind;

    /** The maximum speed allowed on this segment in km/h */
    @Column(name = "max_speed", nullable = false)
    private Integer maxSpeed;

    /** The list of allowed altitude ranges for this segment */
    @ElementCollection
    @CollectionTable(name = "Segment_AltitudeSlots", joinColumns = @JoinColumn(name = "segment_id"))
    private List<AltitudeRange> allowedAltitudes = new ArrayList<>();

    /**
     * Default constructor for ORM use only.
     */
    protected RouteSegment() {
        // for ORM
    }

    /**
     * Creates a new route segment with the specified parameters.
     * 
     * Validates that:
     * - All parameters are non-null
     * - The altitude limits list is not empty
     * - The maximum speed is positive (greater than 0)
     * 
     * @param startNodeCoords the geographical coordinates of the segment's starting point
     * @param endNodeCoords the geographical coordinates of the segment's ending point
     * @param altitudeLimits the list of allowed altitude ranges for this segment
     * @param maxSpeed the maximum speed allowed on this segment in km/h
     * @param wind the wind conditions for this segment
     * 
     * @throws IllegalArgumentException if any parameter is null, if altitude limits is empty,
     *                                   or if maxSpeed is not positive
     */
    public RouteSegment(Coordinates startNodeCoords, Coordinates endNodeCoords, List<AltitudeRange> altitudeLimits, Integer maxSpeed, Wind wind) {

        Preconditions.nonNull(startNodeCoords, "startNodeCoords cannot be null.");
        Preconditions.nonNull(endNodeCoords, "endNodeCoords cannot be null.");
        Preconditions.nonNull(altitudeLimits, "altitudeLimits cannot be null.");
        Preconditions.nonNull(maxSpeed, "maxSpeed cannot be null.");
        Preconditions.nonNull(wind, "wind cannot be null.");

        Preconditions.nonEmpty(altitudeLimits, "altitudeLimits cannot be empty.");

        Preconditions.ensure(maxSpeed > 0, "maxSpeed must be a positive value.");


        this.startNodeCoords = startNodeCoords;
        this.endNodeCoords = endNodeCoords;
        this.allowedAltitudes = new ArrayList<>(allowedAltitudes);
        this.maxSpeed = maxSpeed;
        this.wind = wind;
    }

    /**
     * Returns the identity of this route segment.
     * 
     * @return the unique identifier assigned by the database, or null if not yet persisted
     */
    @Override
    public Long identity() {
        return segmentId;
    }

    /**
     * Checks if this object is the same as another object by comparing identities.
     * 
     * Two RouteSegment objects are the same if they have the same identity.
     * 
     * @param other the object to compare with
     * @return true if both objects represent the same segment, false otherwise
     * 
     * @throws NullPointerException if identity is null for non-persisted segments
     */
    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof RouteSegment)) {
            return false;
        }
        final RouteSegment that = (RouteSegment) other;
        if (this == that) {
            return true;
        }
        return identity().equals(that.identity());
    }
}