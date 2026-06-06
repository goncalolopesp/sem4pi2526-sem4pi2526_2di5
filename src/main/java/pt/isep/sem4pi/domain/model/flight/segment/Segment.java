package pt.isep.sem4pi.domain.model.flight.segment;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import pt.isep.sem4pi.domain.model.airport.Coordinates;

import java.io.Serializable;
import java.util.List;

@Embeddable
public class Segment implements ValueObject, Serializable {

    @Embedded
    private Coordinates startCoordinates;

    @Embedded
    private Coordinates endCoordinates;

    @Embedded
    private List<AltitudeSlots> altitudeSlots;

    @Embedded
    private WindAngle windAngle;

    @Embedded
    private Speed speed;

    protected Segment(){
        //for ORM
    }

    public Segment(Coordinates startCoordinates, Coordinates endCoordinates, List<AltitudeSlots> altitudeSlots, WindAngle windAngle, Speed speed){
        this.startCoordinates = startCoordinates;
        this.endCoordinates = endCoordinates;
        this.altitudeSlots = altitudeSlots;
        this.windAngle = windAngle;
        this.speed = speed;
    }

}
