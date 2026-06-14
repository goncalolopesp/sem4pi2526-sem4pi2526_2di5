package pt.isep.sem4pi.domain.model.flight.segment;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class WindAngle implements ValueObject, Serializable {

    private Double angle;

    protected WindAngle(){
        //for ORM
    }

    public WindAngle(Double angle){
        if (angle < 0){
            angle += 360;
        }

        while (angle > 360){
            angle -= 360;
        }

        this.angle = angle;

    }

    public double angle() {
        return angle;
    }

}
