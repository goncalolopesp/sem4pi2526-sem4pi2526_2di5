package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.CertifiedEngine;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class AircraftModel, an aggregate root of the Domain Model
 */

/**
 * Agregado AircraftModel com regra de certificacao de motores (US57).
 */
@Entity
public class AircraftModel implements AggregateRoot<AircraftModelId> {
    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "aircraft_model_code"))
    private AircraftModelId id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "manufacturer_id"))
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

    @Column(nullable = false)
    private String motorization;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal emptyWeight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumTakeoffWeight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumZeroFuelWeight;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumFuelCapacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceCeiling;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cruiseSpeed;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal wingArea;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal dragCoefficient;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal liftCoefficient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumRange;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal aspectRatio;

    @Version
    private Long version;

    protected AircraftModel(){}

    public AircraftModel(AircraftModelId id, ManufacturerId manufacturerId, AircraftType aircraftType, String modelName, String motorization,
                         BigDecimal emptyWeight, BigDecimal maximumTakeoffWeight, BigDecimal maximumZeroFuelWeight,
                         BigDecimal maximumFuelCapacity, BigDecimal serviceCeiling, BigDecimal cruiseSpeed,
                         BigDecimal wingArea, BigDecimal dragCoefficient, BigDecimal liftCoefficient,
                         BigDecimal maximumRange, BigDecimal aspectRatio) {
        if (id == null) throw new IllegalArgumentException("AircraftModelId cannot be null");
        if (manufacturerId == null) throw new IllegalArgumentException("ManufacturerId cannot be null");
        if (aircraftType == null) throw new IllegalArgumentException("AircraftType cannot be null");
        if (modelName == null || modelName.trim().isEmpty()) throw new IllegalArgumentException("ModelName cannot be null or empty");
        if (motorization == null || motorization.trim().isEmpty()) throw new IllegalArgumentException("Motorization cannot be null or empty");
        validateBigDecimal(emptyWeight, "EmptyWeight");
        validateBigDecimal(maximumTakeoffWeight, "MaximumTakeoffWeight");
        validateBigDecimal(maximumZeroFuelWeight, "MaximumZeroFuelWeight");
        validateBigDecimal(maximumFuelCapacity, "MaximumFuelCapacity");
        validateBigDecimal(serviceCeiling, "ServiceCeiling");
        validateBigDecimal(cruiseSpeed, "CruiseSpeed");
        validateBigDecimal(wingArea, "WingArea");
        validateBigDecimal(dragCoefficient, "DragCoefficient");
        validateBigDecimal(liftCoefficient, "LiftCoefficient");
        validateBigDecimal(maximumRange, "MaximumRange");
        validateBigDecimal(aspectRatio, "AspectRatio");

        this.id = id;
        this.manufacturerId = manufacturerId;
        this.aircraftType = aircraftType;
        this.modelName = modelName;
        this.motorization = motorization;
        this.emptyWeight = emptyWeight;
        this.maximumTakeoffWeight = maximumTakeoffWeight;
        this.maximumZeroFuelWeight = maximumZeroFuelWeight;
        this.maximumFuelCapacity = maximumFuelCapacity;
        this.serviceCeiling = serviceCeiling;
        this.cruiseSpeed = cruiseSpeed;
        this.wingArea = wingArea;
        this.dragCoefficient = dragCoefficient;
        this.liftCoefficient = liftCoefficient;
        this.maximumRange = maximumRange;
        this.aspectRatio = aspectRatio;
    }

    private void validateBigDecimal(BigDecimal value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(fieldName + " must be non-negative");
    }

    /**
     * Regra de negocio: valida compatibilidade e evita duplicados.
     */
    public void certifyEngine(EngineModel engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine cannot be null.");
        }
        if (!isEngineCompatible(engine)) {
            throw new IllegalArgumentException("Engine type is not compatible with aircraft model motorization.");
        }
        if (isEngineAlreadyCertified(engine)) {
            throw new IllegalArgumentException("Engine model already certified for this aircraft model.");
        }
        this.certifiedEngines.add(new CertifiedEngine(engine));
    }

    /**
     * Deteccao de duplicados por modelo de motor.
     */
    private boolean isEngineAlreadyCertified(final EngineModel engine) {
        return certifiedEngines.stream().anyMatch(certifiedEngine -> certifiedEngine.hasEngineModel(engine));
    }

    /**
     * Compatibilidade baseada na motorization do modelo vs tipo do motor.
     */
    private boolean isEngineCompatible(final EngineModel engine) {
        final String motorizationValue = motorization == null ? "" : motorization.trim();
        return !motorizationValue.isEmpty()
                && motorizationValue.equalsIgnoreCase(engine.type().name());
    }

    public String modelName() {
        return modelName;
    }

    public AircraftType aircraftType() {
        return aircraftType;
    }

    public String motorization() {
        return motorization;
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