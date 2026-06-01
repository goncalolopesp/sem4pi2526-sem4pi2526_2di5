package pt.isep.sem4pi.domain.flight;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;

@Entity
@Table(name = "Flight")
public class Flight implements AggregateRoot<FlightId> {

    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "flight_id"))
    private FlightId id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_registration"))
    private AircraftRegistration aircraftRegistration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @Version
    private Long version;

    protected Flight() {
        // for ORM
    }

    public Flight(final FlightId id, final AircraftRegistration aircraftRegistration) {
        if (id == null) {
            throw new IllegalArgumentException("Flight id cannot be null.");
        }
        if (aircraftRegistration == null) {
            throw new IllegalArgumentException("Aircraft registration cannot be null.");
        }
        this.id = id;
        this.aircraftRegistration = aircraftRegistration;
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

