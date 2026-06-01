package pt.isep.sem4pi.domain.airtransportcompany;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AirTransportCompany")
public class AirTransportCompany implements AggregateRoot<IataCodeCompany> {

    @EmbeddedId
    private IataCodeCompany iataCode;

    @Embedded
    private CompanyName name;

    @Embedded
    @Column(unique = true, nullable = false)
    private IcaoCodeCompany icaoCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "company_iata_code")
    private List<Aircraft> fleet = new ArrayList<>();

    @Version
    private Long version;

    protected AirTransportCompany() {
        // For ORM
    }

    public AirTransportCompany(CompanyName name, IataCodeCompany iataCode, IcaoCodeCompany icaoCode) {
        if (name == null || iataCode == null || icaoCode == null) {
            throw new IllegalArgumentException("Company properties cannot be null.");
        }
        this.name = name;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
    }

    public void addAircraftToFleet(Aircraft aircraft) {
        boolean exists = fleet.stream().anyMatch(a -> a.registration().equals(aircraft.registration()));
        if (exists) {
            throw new IllegalStateException("Aircraft already exists in this company's fleet.");
        }
        this.fleet.add(aircraft);
    }

    public List<Aircraft> getFleet() {
        return new ArrayList<>(fleet);
    }

    /* Métodos obrigatórios da interface AggregateRoot da EAPLI */

    @Override
    public IataCodeCompany identity() {
        return this.iataCode;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AirTransportCompany)) {
            return false;
        }
        final AirTransportCompany that = (AirTransportCompany) other;
        if (this == that) {
            return true;
        }
        return identity().equals(that.identity());
    }

    /* Getters gerais para a UI */

    public CompanyName getName() {
        return name;
    }

    public IcaoCodeCompany getIcaoCode() {
        return icaoCode;
    }
}