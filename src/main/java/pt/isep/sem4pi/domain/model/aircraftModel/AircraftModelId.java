package pt.isep.sem4pi.domain.model.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;

/**
 * The ID for Aircraft Model, which is a value object that uniquely identifies an Aircraft Model by its manufacturerId and modelName. It implements Comparable to allow sorting by code.
 */

@Embeddable
public class AircraftModelId implements ValueObject, Comparable<AircraftModelId> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "manufacturer_id"))
    private ManufacturerId manufacturerId;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    protected AircraftModelId() {
        // JPA
    }

    public AircraftModelId(ManufacturerId manufacturerId, String modelName) {
        Preconditions.nonNull(manufacturerId, "ManufacturerId cannot be null.");
        Preconditions.nonEmpty(modelName, "ModelName cannot be empty.");

        this.manufacturerId = manufacturerId;
        this.modelName = modelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftModelId)) return false;
        AircraftModelId that = (AircraftModelId) o;
        return manufacturerId.equals(that.manufacturerId) && modelName.equals(that.modelName);
    }

    @Override
    public int hashCode() {
        return manufacturerId.hashCode() + modelName.hashCode();
    }

    @Override
    public int compareTo(AircraftModelId other) {
        int manCompare = this.manufacturerId.compareTo(other.manufacturerId);
        if (manCompare != 0) return manCompare;
        return this.modelName.compareTo(other.modelName);
    }

    @Override
    public String toString() {
        return "AircraftModelId{" +
                "manufacturerId=" + manufacturerId +
                ", modelName='" + modelName + '\'' +
                '}';
    }
}

//
//@Embedded
//@AttributeOverride(name = "value", column = @Column(name = "manufacturer_id"))
//private Manufacturer manufacturer;
//
//@Column(name = "model_name", nullable = false)
//private String modelName;
//
//protected AircraftModelId() {
//    // JPA
//}
//
//public AircraftModelId(ManufacturerId manufacturer, String modelName) {
//    Preconditions.nonNull(manufacturer, "ManufacturerId cannot be null.");
//    Preconditions.nonEmpty(modelName, "ModelName cannot be empty.");
//
//    this.manufacturer = manufacturer;
//    this.modelName = modelName;
//}