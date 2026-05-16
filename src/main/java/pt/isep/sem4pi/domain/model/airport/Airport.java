package pt.isep.sem4pi.domain.model.airport;

import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.shared.IataCode;
import pt.isep.sem4pi.domain.model.shared.IcaoCode;

@Entity
@Table(name = "Airport")
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long airportId;

    private String name;
    private String town;
    private String country;

    @Embedded
    @Column(unique = true, nullable = false)
    private IataCode iataCode;

    @Embedded
    @Column(unique = true, nullable = false)
    private IcaoCode icaoCode;

    @Embedded
    private Coordinates coordinates;

    @Embedded
    private Elevation elevation;

    @Column(name = "air_control_area_id", nullable = false)
    private String airControlAreaId;

    protected Airport() {}

    public Airport(String name, String town, String country, IataCode iataCode, IcaoCode icaoCode,
                   Coordinates coordinates, Elevation elevation, String airControlAreaId) {
        if (name == null || town == null || country == null || airControlAreaId == null) {
            throw new IllegalArgumentException("Basic airport details cannot be null.");
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

    public IataCode identityIata() { return this.iataCode; }
    public IcaoCode identityIcao() { return this.icaoCode; }
}
