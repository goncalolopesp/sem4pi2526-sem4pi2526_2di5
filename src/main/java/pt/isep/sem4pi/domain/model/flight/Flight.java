package pt.isep.sem4pi.domain.model.flight;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;

import java.time.LocalDate;

@Entity
@Table(name = "Flight")
public class Flight implements AggregateRoot<FlightId> {

    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "flight_id"))
    private FlightId id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_registration"))
    private AircraftRegistration aircraftRegistration;

    @Embedded
    @AttributeOverride(name = "routeId", column = @Column(name = "route_id", nullable = false))
    private FlightRouteID routeId;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @Version
    private Long version;

    protected Flight() {
        // for ORM
    }

    public Flight(final FlightId id, final AircraftRegistration aircraftRegistration,
                  final FlightRouteID routeId, final LocalDate departureDate) {
        Preconditions.nonNull(id, "Flight id cannot be null.");
        Preconditions.nonNull(aircraftRegistration, "Aircraft registration cannot be null.");
        Preconditions.nonNull(routeId, "Flight route ID cannot be null.");
        Preconditions.nonNull(departureDate, "Departure date cannot be null.");

        this.id = id;
        this.aircraftRegistration = aircraftRegistration;
        this.routeId = routeId;
        this.departureDate = departureDate;
        this.status = FlightStatus.PENDING;
    }

    public boolean isPending() {
        return status == FlightStatus.PENDING;
    }

    public FlightStatus status() {
        return status;
    }

    public AircraftRegistration aircraftRegistration() {
        return aircraftRegistration;
    }

    public FlightRouteID routeId() {
        return routeId;
    }

    public LocalDate departureDate() {
        return departureDate;
    }

    @Override
    public FlightId identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Flight)) return false;
        final Flight that = (Flight) other;
        return identity().equals(that.identity());
    }
}