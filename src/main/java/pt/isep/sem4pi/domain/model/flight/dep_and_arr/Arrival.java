package pt.isep.sem4pi.domain.model.flight.dep_and_arr;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;

import java.io.Serializable;

@Embeddable
public class Arrival implements ValueObject, Serializable {

    @Embedded
    private IataCodeAirport arrivalAirport;

    @Embedded
    private Time arrivalTime;

    protected Arrival(){
        //for ORM
    }

    public Arrival(IataCodeAirport airport, Time arrivalTime){
        this.arrivalAirport = airport;
        this.arrivalTime = arrivalTime;
    }

    public IataCodeAirport airport() {
        return arrivalAirport;
    }

    public Time time() {
        return arrivalTime;
    }

}
