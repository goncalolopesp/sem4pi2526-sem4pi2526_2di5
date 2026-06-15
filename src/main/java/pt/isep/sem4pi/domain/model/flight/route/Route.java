package pt.isep.sem4pi.domain.model.flight.route;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Transient;
import pt.isep.sem4pi.domain.model.airport.Coordinates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Route implements ValueObject, Serializable {

    @Embedded
    private RouteID routeID;

    @Transient
    private List<Coordinates> waypoints = new ArrayList<>();

    protected Route(){
        //for ORM
    }

    public Route(RouteID routeID, List<Coordinates> waypoints){

        if (waypoints.size() < 2){
            throw new IllegalArgumentException("Error! The number of waypoints in a route must be equal or higher than 2");
        }

        this.routeID = routeID;
        this.waypoints = waypoints;
    }

}
