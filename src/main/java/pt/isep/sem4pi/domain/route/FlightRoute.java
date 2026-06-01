package pt.isep.sem4pi.domain.route;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.airport.Coordinates;
import pt.isep.sem4pi.domain.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.airtransportcompany.IataCodeCompany;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FlightRoute")
public class FlightRoute implements AggregateRoot<FlightRouteID> {

    @EmbeddedId
    private FlightRouteID routeID;

    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "company_iata_code", nullable = false))
    private IataCodeCompany companyCode;

    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "origin_airport_iata", nullable = false))
    private IataCodeAirport originAirport;

    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "destination_airport_iata", nullable = false))
    private IataCodeAirport destinationAirport;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "route_id", nullable = false)
    private List<RouteSegment> segments = new ArrayList<>();

    @Version
    private Long version;

    protected FlightRoute(){
        // for ORM
    }

    public FlightRoute(FlightRouteID routeID, IataCodeCompany companyCode, IataCodeAirport originAirport, IataCodeAirport destinationAirport, List<RouteSegment> segments){

        Preconditions.nonNull(routeID, "Route ID cannot be null.");
        Preconditions.nonNull(companyCode, "Company Code cannot be null.");
        Preconditions.nonNull(originAirport, "Origin Airport cannot be null.");
        Preconditions.nonNull(destinationAirport, "Destination Airport cannot be null.");
        Preconditions.nonNull(segments, "Waypoints cannot be null.");

        Preconditions.nonEmpty(segments, "Waypoints cannot be empty.");

        // US073 Validation: Name format and company initials match
        String routeName = routeID.code();
        String companyInitials = companyCode.code();

        if (!routeName.startsWith(companyInitials)) {
            throw new IllegalArgumentException("Route name must start with the company's IATA code initials.");
        }

        if (!routeName.matches("^[A-Za-z]{2}\\d{1,4}$")) {
            throw new IllegalArgumentException("Route name must consist of exactly 2 letters followed by up to 4 numbers.");
        }

        this.routeID = routeID;
        this.companyCode = companyCode;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.segments = new ArrayList<>(segments);
    }

    @Override
    public FlightRouteID identity() {
        return this.routeID;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof FlightRoute)) {
            return false;
        }
        final FlightRoute that = (FlightRoute) other;
        if (this == that) {
            return true;
        }
        return identity().equals(that.identity());
    }
}