package pt.isep.sem4pi.domain.model.flight.fuel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;

@Embeddable
public class Fuel implements ValueObject, Serializable {

    @Embedded
    private FuelCapacity fuelCapacity;

    @Enumerated(EnumType.STRING)
    private FuelUnit fuelUnit;

    protected Fuel(){
        //for ORM
    }

    public Fuel(FuelCapacity fuelCapacity, FuelUnit fuelUnit){
        this.fuelCapacity = fuelCapacity;
        this.fuelUnit = fuelUnit;
    }
}
