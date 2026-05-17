package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
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
    private AircraftModelId id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "aircraft_manufacturer_id", referencedColumnName = "manufacturer_id"),
            @JoinColumn(name = "aircraft_model_name", referencedColumnName = "model_name")
    })
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
                         WingGeometry wingGeometry, List<EngineModel> engineModel) {

        // EAPLI Preconditions
        Preconditions.nonNull(id, "AircraftModelId cannot be null.");
        Preconditions.nonNull(aircraftType, "AircraftType cannot be null.");
        Preconditions.nonNull(motorization, "Motorization cannot be null.");
        Preconditions.nonNull(fuel, "Fuel cannot be null.");
        Preconditions.nonNull(wingGeometry, "Wing geometry cannot be null.");
        Preconditions.nonNull(performance, "AircraftPerformance cannot be null.");
        Preconditions.nonNull(weightLimits, "Weight limits cannot be null.");
        Preconditions.nonNull(aerodynamics, "Aerodynamics cannot be null.");
        Preconditions.ensure(engineModel != null && !engineModel.isEmpty(), "Should give at least one Engine Model.");

        this.id = id;
        this.aircraftType = aircraftType;
        this.motorization = motorization;
        this.fuel = fuel;
        this.wingGeometry = wingGeometry;
        this.performance = performance;
        this.weightLimits = weightLimits;
        this.aerodynamics = aerodynamics;

        for (EngineModel engine : engineModel) {
            this.certifyEngine(engine);
        }
    }

    public void certifyEngine(EngineModel engine) {
        Preconditions.nonNull(engine, "Engine cannot be null.");

        boolean alreadyCertified = this.certifiedEngines.stream()
                .anyMatch(ce -> ce.engineModel().identity().equals(engine.identity()));

        Preconditions.ensure(!alreadyCertified,
                "This engine is already certified for this model.");

        this.certifiedEngines.add(new CertifiedEngine(engine));
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AircraftModel)) return false;
        final AircraftModel that = (AircraftModel) other;
        return this.identity().equals(that.identity());
    }

    @Override
    public AircraftModelId identity() {
        return id;
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