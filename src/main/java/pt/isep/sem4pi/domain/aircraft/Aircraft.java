package pt.isep.sem4pi.domain.aircraft;

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

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "Aircraft")
public class Aircraft implements AggregateRoot<AircraftRegistration> {

    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_registration"))
    private AircraftRegistration id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_model_id"))
    private AircraftModelId modelId;

    @Embedded
    private CabinConfiguration seats;

    @Column(name = "registration_country", nullable = false)
    private String registrationCountry;

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;

    @Version
    private Long version;

    protected Aircraft() {
        // for ORM
    }

    public Aircraft(final AircraftRegistration registration, final AircraftModelId modelId,
                    final CabinConfiguration seats, final String registrationCountry,
                    final LocalDate manufacturingDate) {

        if (registration == null) {
            throw new IllegalArgumentException("Aircraft registration cannot be null.");
        }
        if (modelId == null) {
            throw new IllegalArgumentException("Aircraft model id cannot be null.");
        }
        if (seats == null) {
            throw new IllegalArgumentException("Cabin configuration cannot be null.");
        }
        if (registrationCountry == null || registrationCountry.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration country cannot be null or empty.");
        }
        if (manufacturingDate == null || manufacturingDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Manufacturing date cannot be null or in the future.");
        }

        this.id = registration;
        this.modelId = modelId;
        this.seats = seats;
        this.registrationCountry = registrationCountry;
        this.manufacturingDate = manufacturingDate;
        this.status = AircraftStatus.ACTIVE;
    }

    /* Calculates the aircraft age dynamically based on the current date */
    public int age() {
        return Period.between(this.manufacturingDate, LocalDate.now()).getYears();
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

    public CabinConfiguration seats() {
        return seats;
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