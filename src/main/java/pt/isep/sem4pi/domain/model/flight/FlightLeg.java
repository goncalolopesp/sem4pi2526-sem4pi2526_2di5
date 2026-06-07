package pt.isep.sem4pi.domain.model.flight;

import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.route.Route;
import pt.isep.sem4pi.domain.model.flight.segment.Segment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "FlightLeg")
public class FlightLeg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "departureAirport.iataCode", column = @Column(name = "departure_airport_iata")),
            @AttributeOverride(name = "departureDate.day", column = @Column(name = "departure_day")),
            @AttributeOverride(name = "departureDate.month", column = @Column(name = "departure_month")),
            @AttributeOverride(name = "departureDate.year", column = @Column(name = "departure_year")),
            @AttributeOverride(name = "departureTime.hour", column = @Column(name = "departure_hour")),
            @AttributeOverride(name = "departureTime.minute", column = @Column(name = "departure_minute")),
            @AttributeOverride(name = "departureTime.second", column = @Column(name = "departure_second"))
    })
    private Departure departure;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "arrivalAirport.iataCode", column = @Column(name = "arrival_airport_iata")),
            @AttributeOverride(name = "arrivalTime.hour", column = @Column(name = "arrival_hour")),
            @AttributeOverride(name = "arrivalTime.minute", column = @Column(name = "arrival_minute")),
            @AttributeOverride(name = "arrivalTime.second", column = @Column(name = "arrival_second"))
    })
    private Arrival arrival;

    @Transient
    private List<Segment> segments = new ArrayList<>();

    @Embedded
    private Fuel fuel;

    protected FlightLeg(){
        //for ORM
    }

    public FlightLeg(Departure dp, Arrival ar, List<Segment> segmentList, Fuel f){
        this.departure = dp;
        this.arrival = ar;
        if (segmentList != null) {
            this.segments = new ArrayList<>(segmentList);
        }
        this.fuel = f;
    }

    public Departure departure() {
        return departure;
    }

    public Arrival arrival() {
        return arrival;
    }

    public Fuel fuel() {
        return fuel;
    }

    public List<Segment> segments() {
        return Collections.unmodifiableList(segments);
    }
}
