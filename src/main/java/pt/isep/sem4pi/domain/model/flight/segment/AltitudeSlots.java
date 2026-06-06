package pt.isep.sem4pi.domain.model.flight.segment;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class AltitudeSlots implements ValueObject, Serializable {

    private Integer minimumHeight;
    private Integer maximumHeight;

    protected AltitudeSlots(){
        //for ORM
    }

    public AltitudeSlots(Integer minimumHeight, Integer maximumHeight){
        if (minimumHeight < 0){
            throw new IllegalArgumentException("Minimum height cannot be negative");
        }
        if (maximumHeight < 0){
            throw new IllegalArgumentException("Maximum height cannot be negative");
        }
        if (maximumHeight < minimumHeight){
            throw new IllegalArgumentException("Maximum height cannot be lower than the minimum height");
        }

        this.minimumHeight = minimumHeight;
        this.maximumHeight = maximumHeight;
    }

}
