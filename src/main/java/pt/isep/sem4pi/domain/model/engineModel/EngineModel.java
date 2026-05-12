package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import pt.isep.sem4pi.domain.model.Manufacture.Manufacturer;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EngineType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal power;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal efficiency;

    @Version
    private Long version;

    protected EngineModel() {
        // for ORM
    }

    public EngineModel(EngineModelId id, Manufacturer manufacturer, String name, EngineType type, BigDecimal power,
                       FuelType fuelType, BigDecimal efficiency) {
        Preconditions.noneNull(id, manufacturer, type, fuelType, power, efficiency);
        Preconditions.ensure(name != null && !name.trim().isEmpty(),
                "The Engine Model name cannot be null or empty!");
        Preconditions.ensure(power.compareTo(BigDecimal.ZERO) > 0,
                "The Engine Model power must be greater than zero!");
        Preconditions.ensure(efficiency.compareTo(BigDecimal.ZERO) > 0,
                "The Engine Model efficiency must be greater than zero!");
        this.id = id;
        this.manufacturer = manufacturer;
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
        return this.manufacturer.identity().equals(manufacturerId)
                && this.name.equalsIgnoreCase(name.trim());
    }
}
