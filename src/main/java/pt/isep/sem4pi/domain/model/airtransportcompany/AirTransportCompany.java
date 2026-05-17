package pt.isep.sem4pi.domain.model.airtransportcompany;

import jakarta.persistence.*;

@Entity
@Table(name = "AirTransportCompany")
public class AirTransportCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Embedded
    private CompanyName name;

    @Embedded
    @Column(unique = true, nullable = false)
    private IataCode iataCode;

    @Embedded
    @Column(unique = true, nullable = false)
    private IcaoCode icaoCode;

    protected AirTransportCompany() {
        // For ORM
    }

    public AirTransportCompany(CompanyName name, IataCode iataCode, IcaoCode icaoCode) {
        if (name == null || iataCode == null || icaoCode == null) {
            throw new IllegalArgumentException("Company properties cannot be null.");
        }
        this.name = name;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
    }

    public IataCode identityIata() {
        return this.iataCode;
    }

    public IcaoCode identityIcao() {
        return this.icaoCode;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public CompanyName getName() {
        return name;
    }

    public IataCode getIataCode() {
        return iataCode;
    }

    public IcaoCode getIcaoCode() {
        return icaoCode;
    }
}