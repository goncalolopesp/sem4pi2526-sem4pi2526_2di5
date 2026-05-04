package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.CertifiedEngine;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class AircraftModel, an aggregate root of the Domain Model
 */

@Entity

@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "UC_AIRCRAFTMODEL_MANUFACTURER_NAME",
                columnNames = {"manufacturerId", "modelName"}
        )
})

public class AircraftModel implements AggregateRoot<AircraftModelId> {
    @EmbeddedId
    private AircraftModelId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "manufacturer_id"))
    private ManufacturerId manufacturerId;

    // Relationship with CertifiedEngine (Entities inside this Aggregate)
    // We use OneToMany with Cascade because the lifecycle is tied to the AircraftModel
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "aircraft_model_id")
    private List<CertifiedEngine> certifiedEngines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AircraftType aircraftType;

    @Column(nullable = false)
    private String modelName;

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

    protected AircraftModel(){}

    public AircraftModel(AircraftModelId id, ManufacturerId manufacturerId, AircraftType aircraftType,
                         String modelName, MotorizationType motorization,
                         Weight weightLimits, Coefficient aerodynamics,
                         AircraftPerformance performance, Fuel fuel,
                         WingGeometry wingGeometry,
                         List<EngineModelId> engineModelIds) {

        if (id == null) throw new IllegalArgumentException("AircraftModelId cannot be null");
        if (manufacturerId == null) throw new IllegalArgumentException("ManufacturerId cannot be null");
        if (aircraftType == null) throw new IllegalArgumentException("AircraftType cannot be null");
        if (modelName == null || modelName.trim().isEmpty()) throw new IllegalArgumentException("ModelName cannot be null or empty");
        if (motorization == null) throw new IllegalArgumentException("Motorization cannot be null");

        if (fuel == null) throw new IllegalArgumentException("Fuel cannot be null");
        if (wingGeometry == null) throw new IllegalArgumentException("Wing geometry cannot be null");
        if (performance == null) throw new IllegalArgumentException("AircraftPerformance cannot be null");
        if (weightLimits == null) throw new IllegalArgumentException("Weight limits cannot be null");
        if (aerodynamics == null) throw new IllegalArgumentException("Aerodynamics coefficients cannot be null");

        if (engineModelIds == null || engineModelIds.isEmpty()) throw new IllegalArgumentException("At least one Engine Model ID must be provided");

        this.id = id;
        this.manufacturerId = manufacturerId;
        this.aircraftType = aircraftType;
        this.modelName = modelName;
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

    // Business method to ensure invariants
    public void certifyEngine(EngineModelId engineId) {
        if (engineId == null) {
            throw new IllegalArgumentException("Engine ID cannot be null.");
        }

        /// Business rule: Ensure that doesn't insert the same engine twice in the same aircraft
        boolean alreadyCertified = this.certifiedEngines.stream()
                .anyMatch(ce -> ce.engineModelId().equals(engineId));

        if (alreadyCertified) {
            throw new IllegalArgumentException("This engine is already certified for this aircraft model.");
        }

        this.certifiedEngines.add(new CertifiedEngine(engineId));
    }

    public AircraftModelId id() {return this.id;}
    public ManufacturerId manufacturerId() {return this.manufacturerId;}
    public AircraftType aircraftType() {return this.aircraftType;}
    public String modelName() {return this.modelName;}
    public MotorizationType motorization() {return this.motorization;}
    public Fuel fuel() {return this.fuel;}
    public WingGeometry wingGeometry() {return this.wingGeometry;}
    public Coefficient aerodynamics() {return this.aerodynamics;}
    public AircraftPerformance performance() {return this.performance;}
    public Weight weightLimits() {return this.weightLimits;}
    public List<EngineModelId> engineModelIds() {
        List<EngineModelId> engineIds = new ArrayList<>();
        for (CertifiedEngine ce : this.certifiedEngines) {
            engineIds.add(ce.engineModelId());
        }
        return engineIds;
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
}
