package pt.isep.sem4pi.domain.model.airport;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;

@Entity
@Table(name = "Airport")
public class Airport implements AggregateRoot<IataCodeAirport> {

    @EmbeddedId
    private IataCodeAirport iataCode;

    @Embedded
    @Column(unique = true, nullable = false)
    private IcaoCodeAirport icaoCode;

    private String name;
    private String town;
    private String country;

    @Embedded
    private Coordinates coordinates;

    @Embedded
    private Elevation elevation;

    @Embedded
    @AttributeOverride(name = "areaCode", column = @Column(name = "air_control_area_id", nullable = false))
    private AirControlAreaID airControlAreaId;

    @Version
    private Long version;

    protected Airport() {
        // For ORM
    }

    public Airport(String name, String town, String country, IataCodeAirport iataCode, IcaoCodeAirport icaoCode,
                   Coordinates coordinates, Elevation elevation, AirControlAreaID airControlAreaId) {
        if (name == null || town == null || country == null || airControlAreaId == null || iataCode == null || icaoCode == null) {
            throw new IllegalArgumentException("Airport details cannot be null.");
        }
        this.name = name;
        this.town = town;
        this.country = country;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.coordinates = coordinates;
        this.elevation = elevation;
        this.airControlAreaId = airControlAreaId;
    }

    @Override
    public IataCodeAirport identity() {
        return this.iataCode;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof Airport)) {
            return false;
        }
        final Airport that = (Airport) other;
        if (this == that) {
            return true;
        }
        return identity().equals(that.identity());
    }

    /* Getters gerais para a UI e lógicas de domínio */

    public IcaoCodeAirport identityIcao() { return this.icaoCode; }

    public String getName() { return name; }

    public String getTown() { return town; }

    public String getCountry() { return country; }

    public Coordinates getCoordinates() { return coordinates; }

    public Elevation getElevation() { return elevation; }

    public AirControlAreaID getAirControlAreaId() { return airControlAreaId; }
}