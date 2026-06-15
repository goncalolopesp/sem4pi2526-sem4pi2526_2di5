package pt.isep.sem4pi.domain.model.flight.dep_and_arr;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;

import java.io.Serializable;

@Embeddable
public class Departure implements ValueObject, Serializable {

    @Embedded
    private IataCodeAirport departureAirport;
    @Embedded
    private Date departureDate;
    @Embedded
    private Time departureTime;

    protected Departure(){
        //for ORM
    }

    public Departure(IataCodeAirport airport, Date depDate, Time depTime){
        this.departureAirport = airport;
        this.departureDate = depDate;
        this.departureTime = depTime;
    }

    public IataCodeAirport airport() {
        return departureAirport;
    }

    public Date date() {
        return departureDate;
    }

    public Time time() {
        return departureTime;
    }

}
