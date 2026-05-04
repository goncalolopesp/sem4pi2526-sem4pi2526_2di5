package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pt.isep.sem4pi.domain.model.Manufacture.ManufacturerId;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "engine_model", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"manufacturer_id", "name"})
})
public class EngineModel implements AggregateRoot<EngineModelId>, Serializable {

    @EmbeddedId
    private EngineModelId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "manufacturer_id"))
    private ManufacturerId manufacturerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal power;

    @Column(nullable = false)
    private String fuelType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal efficiency;

    protected EngineModel() {
        // for ORM
    }

    public EngineModel(EngineModelId id, ManufacturerId manufacturerId, String name, String type, BigDecimal power, String fuelType, BigDecimal efficiency) {
        if (id == null) {
            throw new IllegalArgumentException("The Engine Model ID cannot be null!");
        }
        if (manufacturerId == null) {
            throw new IllegalArgumentException("The Manufacturer ID cannot be null!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The Engine Model name cannot be null or empty!");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("The Engine Model type cannot be null or empty!");
        }
        if (power == null || power.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The Engine Model power must be greater than zero!");
        }
        if (fuelType == null || fuelType.trim().isEmpty()) {
            throw new IllegalArgumentException("The Engine Model fuel type cannot be null or empty!");
        }
        if (efficiency == null || efficiency.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The Engine Model efficiency must be greater than zero!");
        }
        this.id = id;
        this.manufacturerId = manufacturerId;
        this.name = name;
        this.type = type;
        this.power = power;
        this.fuelType = fuelType;
        this.efficiency = efficiency;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof EngineModel)) return false;
        final EngineModel that = (EngineModel) other;

        return this.identity().equals(that.identity());
    }

    @Override
    public EngineModelId identity() {
        return id;
    }

    public boolean hasManufacturerAndName(final ManufacturerId manufacturerId, final String name) {
        if (manufacturerId == null || name == null || name.trim().isEmpty()) {
            return false;
        }
        return this.manufacturerId.equals(manufacturerId) && this.name.equalsIgnoreCase(name.trim());
    }
}
