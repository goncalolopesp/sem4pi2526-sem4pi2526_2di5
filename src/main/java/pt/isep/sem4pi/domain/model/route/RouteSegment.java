package pt.isep.sem4pi.domain.model.route;

import eapli.framework.domain.model.DomainEntity;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.airport.Coordinates;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RouteSegment")
public class RouteSegment implements DomainEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long segmentId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_lon"))
    })
    private Coordinates startNodeCoords;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_lon"))
    })
    private Coordinates endNodeCoords;

    @Embedded
    private Wind wind;

    @Column(name = "max_speed", nullable = false)
    private Integer maxSpeed;

    @ElementCollection
    @CollectionTable(name = "Segment_AltitudeSlots", joinColumns = @JoinColumn(name = "segment_id"))
    private List<AltitudeRange> allowedAltitudes = new ArrayList<>();

    protected RouteSegment() {
        // for ORM
    }

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

    public Coordinates startNode() {
        return startNodeCoords;
    }

    public Coordinates endNode() {
        return endNodeCoords;
    }

    @Override
    public Long identity() {
        return segmentId;
    }

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