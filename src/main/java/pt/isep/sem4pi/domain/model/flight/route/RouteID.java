package pt.isep.sem4pi.domain.model.flight.route;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RouteID implements ValueObject, Serializable {

    private Integer routeID;

    protected RouteID(){
        //for ORM
    }

    public RouteID(Integer routeID){
        this.routeID = routeID;
    }

}
