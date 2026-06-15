package pt.isep.sem4pi.domain.model.flight.fuel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FuelCapacity implements ValueObject, Serializable {

    private Double capacity;

    protected FuelCapacity(){
        //for ORM
    }

    public FuelCapacity(Double capacity){
        if (capacity == null || capacity <= 0){
            throw new IllegalArgumentException("Fuel quantity must be strictly positive");
        }

        this.capacity = capacity;
    }

    public Double value() {
        return capacity;
    }

}
