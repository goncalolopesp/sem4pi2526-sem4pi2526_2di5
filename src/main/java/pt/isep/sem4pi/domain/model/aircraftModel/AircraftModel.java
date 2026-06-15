package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.representations.RepresentationBuilder;
import eapli.framework.representations.Representationable;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.AircraftModelDTO;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.CertifiedEngineDTO;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.CertifiedEngine;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;

import java.util.ArrayList;
import java.util.List;

/**
 * Class AircraftModel, an aggregate root of the Domain Model
 */

/**
 * Agregado AircraftModel com regra de certificacao de motores (US57).
 */
@Entity

public class AircraftModel implements AggregateRoot<AircraftModelId>, Representationable {

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
    private EngineType motorization;

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

    @Column(name = "maximum_capacity")
    private Integer maximumCapacity;

    @Version
    private Long version;

    protected AircraftModel() {
        // JPA
    }

    public AircraftModel(AircraftModelId id, AircraftType aircraftType, EngineType motorization,
                         Weight weightLimits, Coefficient aerodynamics,
                         AircraftPerformance performance, Fuel fuel,
                         WingGeometry wingGeometry, List<EngineModel> engineModel,
                         Integer maximumCapacity) {

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
        Preconditions.nonNull(maximumCapacity, "Maximum capacity cannot be null.");
        Preconditions.ensure(maximumCapacity > 0, "Maximum capacity must be positive.");

        this.id = id;
        this.aircraftType = aircraftType;
        this.motorization = motorization;
        this.fuel = fuel;
        this.wingGeometry = wingGeometry;
        this.performance = performance;
        this.weightLimits = weightLimits;
        this.aerodynamics = aerodynamics;
        this.maximumCapacity = maximumCapacity;

        for (EngineModel engine : engineModel) {
            this.certifyEngine(engine);
        }
    }

    /**
     * Regra de negocio: valida compatibilidade e evita duplicados.
     */
    public void certifyEngine(EngineModel engine) {
        Preconditions.nonNull(engine, "Engine cannot be null.");
        Preconditions.ensure(isEngineCompatible(engine),
                "Engine type is not compatible with this aircraft's motorization.");
        Preconditions.ensure(!isEngineAlreadyCertified(engine),
                "This engine is already certified for this model.");

        this.certifiedEngines.add(new CertifiedEngine(engine));
    }

    /**
     * Deteccao de duplicados por modelo de motor.
     */
    private boolean isEngineAlreadyCertified(final EngineModel engine) {
        return certifiedEngines.stream()
                .anyMatch(ce -> ce.hasEngineModel(engine));
    }

    /**
     * Compatibilidade baseada na motorization do modelo vs tipo do motor.
     */
    private boolean isEngineCompatible(final EngineModel engine) {
        if (motorization == null || engine.type() == null) {
            return false;
        }
        return motorization == engine.type();
    }

    public String modelName() {
        return id.toString();
    }
    public AircraftType aircraftType() {
        return aircraftType;
    }
    public EngineType motorization() {
        return motorization;
    }
    public Fuel fuel() {return fuel;}
    public WingGeometry wingGeometry() {return wingGeometry;}
    public Coefficient aerodynamics() {return aerodynamics;}
    public AircraftPerformance performance() {return performance;}
    public Weight weightLimits() {return weightLimits;}
    public List<CertifiedEngine> certifiedEngines() {return certifiedEngines;}
    public Long version() {return version;}

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

    @Override
    public <R> R buildRepresentation(final RepresentationBuilder<R> builder) {
        builder.startObject("AircraftModel")
                .withProperty("manufacturerId", id.manufacturerId().toString())
                .withProperty("modelName", id.modelName())
                .withProperty("aircraftType", aircraftType.name())
                .withProperty("motorization", motorization.name())
                .withProperty("maximumCapacity", maximumCapacity)
                .withProperty("version", version);

        if (weightLimits != null) {
            builder.startObject("weightLimits")
                    .withProperty("emptyWeight", weightLimits.emptyWeight().doubleValue())
                    .withProperty("maxZeroFuelWeight", weightLimits.maxZeroFuelWeight().doubleValue())
                    .withProperty("maxTakeOffWeight", weightLimits.maxTakeOffWeight().doubleValue())
                    .endObject();
        }

        if (aerodynamics != null) {
            builder.startObject("aerodynamics")
                    .withProperty("dragCoefficient", aerodynamics.dragCoefficient().doubleValue())
                    .withProperty("liftCoefficient", aerodynamics.liftCoefficient().doubleValue())
                    .endObject();
        }

        if (fuel != null) {
            builder.startObject("fuel")
                    .withProperty("maximumFuelCapacity", fuel.maxFuelCapacity().doubleValue())
                    .endObject();
        }

        if (wingGeometry != null) {
            builder.startObject("wingGeometry")
                    .withProperty("wingArea", wingGeometry.wingArea().doubleValue())
                    .withProperty("aspectRatio", wingGeometry.aspectRatio().doubleValue())
                    .endObject();
        }

        if (performance != null) {
            builder.startObject("performance")
                    .withProperty("serviceCeiling", performance.serviceCeiling().doubleValue())
                    .withProperty("cruiseSpeed", performance.cruiseSpeed().doubleValue())
                    .withProperty("maximumRangeKm", performance.maximumRangeKm().doubleValue())
                    .endObject();
        }

        builder.startCollection("certifiedEngines");
        for (CertifiedEngine ce : this.certifiedEngines) {
            ce.buildRepresentation(builder);
        }
        builder.endCollection();

        builder.endObject();
        return builder.build();
    }
}