package pt.isep.sem4pi.domain.model.pilot;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;

@Entity
@Table(name = "Pilot")
public class Pilot implements AggregateRoot<PilotId> {

    @EmbeddedId
    private PilotId pilotId;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "company_iata_code", nullable = false))
    private IataCodeCompany companyCode;

    protected Pilot(){
        //for ORM
    }

    public Pilot(PilotId pilotId, IataCodeCompany companyCode){
        Preconditions.nonNull(pilotId, "Pilot id is required");
        Preconditions.nonNull(companyCode, "Company is required");
        this.pilotId = pilotId;
        this.companyCode = companyCode;
    }

    public IataCodeCompany companyCode() {
        return companyCode;
    }

    @Override
    public boolean sameAs(Object other){
        if (!(other instanceof Pilot)){
            return false;
        }
        Pilot otherPilot = (Pilot) other;
        return identity().equals(otherPilot.identity());
    }

    @Override
    public PilotId identity() {
        return pilotId;
    }

}
