package pt.isep.sem4pi.domain.model.aircraft;

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
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;

@Entity
@Table(name = "Aircraft")
public class Aircraft implements AggregateRoot<AircraftRegistration> {

    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_registration"))
    private AircraftRegistration id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_model_id"))
    private AircraftModelId modelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;

    @Version
    private Long version;

    protected Aircraft() {
        // for ORM
    }

    public Aircraft(final AircraftRegistration registration, final AircraftModelId modelId) {
        if (registration == null) {
            throw new IllegalArgumentException("Aircraft registration cannot be null.");
        }
        if (modelId == null) {
            throw new IllegalArgumentException("Aircraft model id cannot be null.");
        }
        this.id = registration;
        this.modelId = modelId;
        this.status = AircraftStatus.ACTIVE;
    }

    public void retire() {
        if (status == AircraftStatus.RETIRED) {
            throw new IllegalStateException("Aircraft is already retired.");
        }
        this.status = AircraftStatus.RETIRED;
    }

    public boolean isRetired() {
        return status == AircraftStatus.RETIRED;
    }

    public AircraftStatus status() {
        return status;
    }

    public AircraftModelId modelId() {
        return modelId;
    }

    public AircraftRegistration registration() {
        return id;
    }

    @Override
    public AircraftRegistration identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Aircraft)) return false;
        final Aircraft that = (Aircraft) other;
        return identity().equals(that.identity());
    }
}

