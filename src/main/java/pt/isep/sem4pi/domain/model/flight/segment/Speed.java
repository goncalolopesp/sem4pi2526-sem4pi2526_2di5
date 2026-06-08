package pt.isep.sem4pi.domain.model.flight.segment;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class Speed implements ValueObject, Serializable {

    private Double mps;

    protected Speed(){
        //for ORM
    }

    public Speed(Double mps){
        if (mps < 0){
            throw new IllegalArgumentException("Speed cannot be negative");
        }

        this.mps = mps;

    }

}
