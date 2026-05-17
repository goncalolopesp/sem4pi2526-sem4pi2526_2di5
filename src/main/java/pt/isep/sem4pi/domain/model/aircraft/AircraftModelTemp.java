package pt.isep.sem4pi.domain.model.aircraft;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "AircraftModelTemp")
public class AircraftModelTemp {

    @Id
    private String modelId;
    private int maximumCapacity;

    protected AircraftModelTemp() {}

    public AircraftModelTemp(String modelId, int maximumCapacity) {
        this.modelId = modelId;
        this.maximumCapacity = maximumCapacity;
    }

    public String getModelId() { return modelId; }
    public int getMaximumCapacity() { return maximumCapacity; }
}