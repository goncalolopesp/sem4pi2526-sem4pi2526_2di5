package pt.isep.sem4pi.domain.aircraftModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class AircraftModelId implements ValueObject {
    private String code;
    protected AircraftModelId(){}
}
