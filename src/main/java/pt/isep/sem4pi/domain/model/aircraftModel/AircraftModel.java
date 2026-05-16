package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.CertifiedEngine;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

import java.util.ArrayList;
import java.util.List;

/**
 * Class AircraftModel, an aggregate root of the Domain Model
 */

@Entity

public class AircraftModel implements AggregateRoot<AircraftModelId> {

    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_model_code"))
    private AircraftModelId id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "aircraft_model_id")
    private List<CertifiedEngine> certifiedEngines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AircraftType aircraftType;

    @Enumerated(EnumType.STRING)
    private MotorizationType motorization;

    @Embedded
    private Fuel fuel;

    @Embedded
    private WingGeometry wingGeometry;

    @Embedded
    private AircraftPerformance performance;

    @Embedded
    private Weight weightLimits;

    @Embedded
    private Coefficient aerodynamics;

    @Version
    private Long version;

    protected AircraftModel() {
        // JPA
    }

    public AircraftModel(AircraftModelId id, AircraftType aircraftType, MotorizationType motorization,
                         Weight weightLimits, Coefficient aerodynamics,
                         AircraftPerformance performance, Fuel fuel,
                         WingGeometry wingGeometry, List<EngineModelId> engineModelIds) {

        // EAPLI Preconditions
        Preconditions.nonNull(id, "AircraftModelId não pode ser nulo.");
        Preconditions.nonNull(aircraftType, "AircraftType não pode ser nulo.");
        Preconditions.nonNull(motorization, "Motorization não pode ser nulo.");
        Preconditions.nonNull(fuel, "Fuel não pode ser nulo.");
        Preconditions.nonNull(wingGeometry, "Wing geometry não pode ser nulo.");
        Preconditions.nonNull(performance, "AircraftPerformance não pode ser nulo.");
        Preconditions.nonNull(weightLimits, "Weight limits não pode ser nulo.");
        Preconditions.nonNull(aerodynamics, "Aerodynamics não pode ser nulo.");
        Preconditions.ensure(engineModelIds != null && !engineModelIds.isEmpty(), "Deve fornecer pelo menos um Engine Model ID.");

        this.id = id;
        this.aircraftType = aircraftType;
        this.motorization = motorization;
        this.fuel = fuel;
        this.wingGeometry = wingGeometry;
        this.performance = performance;
        this.weightLimits = weightLimits;
        this.aerodynamics = aerodynamics;

        for (EngineModelId engine : engineModelIds) {
            this.certifyEngine(engine);
        }
    }

    public void certifyEngine(EngineModelId engineId) {
        Preconditions.nonNull(engineId, "Engine ID não pode ser nulo.");

        boolean alreadyCertified = this.certifiedEngines.stream()
                .anyMatch(ce -> ce.engineModelId().equals(engineId));

        Preconditions.ensure(!alreadyCertified, "Este motor já está certificado para este modelo.");

        this.certifiedEngines.add(new CertifiedEngine(engineId));
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AircraftModel)) return false;
        final AircraftModel that = (AircraftModel) other;
        return this.identity().equals(that.identity());
    }

    @Override
    public AircraftModelId identity() {
        return null;
    }

    @Override
    public String toString() {
        return "AircraftModel{" +
                "id=" + id +
                ", certifiedEngines=" + certifiedEngines +
                ", aircraftType=" + aircraftType +
                ", motorization=" + motorization +
                ", fuel=" + fuel +
                ", wingGeometry=" + wingGeometry +
                ", performance=" + performance +
                ", weightLimits=" + weightLimits +
                ", aerodynamics=" + aerodynamics +
                ", version=" + version +
                '}';
    }
}