package pt.isep.sem4pi.domain.airtransportcompany;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import jakarta.persistence.*;

@Entity
public class AirTransportCompanyCollaborator implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private SystemUser systemUser;

    @ManyToOne(optional = false)
    private AirTransportCompany company;

    protected AirTransportCompanyCollaborator() {
        // For ORM
    }

    public AirTransportCompanyCollaborator(SystemUser systemUser, AirTransportCompany company) {
        if (systemUser == null || company == null) {
            throw new IllegalArgumentException("System User and Company cannot be null.");
        }
        this.systemUser = systemUser;
        this.company = company;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AirTransportCompanyCollaborator that)) {
            return false;
        }
        if (this.identity() == null || that.identity() == null) {
            return this == other;
        }
        return this.identity().equals(that.identity());
    }

    public void assignIdentity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        if (this.id != null) {
            throw new IllegalStateException("Identity already assigned.");
        }
        this.id = id;
    }

    @Override
    public Long identity() {
        return this.id;
    }

    public SystemUser systemUser() {
        return this.systemUser;
    }

    public AirTransportCompany company() {
        return this.company;
    }
}