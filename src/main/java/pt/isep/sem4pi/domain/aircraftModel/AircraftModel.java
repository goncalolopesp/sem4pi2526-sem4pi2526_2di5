package pt.isep.sem4pi.domain.aircraftModel;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.security.Identity;

@Entity
public class AircraftModel implements AggregateRoot<AircraftModelId> {
    @EmbeddedId
    private AircraftModelId id;

    @Version
    private Long version;

    protected AircraftModel(){}

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
}
