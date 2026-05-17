package pt.isep.sem4pi.domain.aircontrolarea;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

@Entity
public class AirControlArea implements AggregateRoot<AirControlAreaID> {

    @Version
    private Long version;

    @EmbeddedId
    private AirControlAreaID acID;

    @Embedded
    private AirControlAreaBoundary airControlAreaBoundaries;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double minimumReserveFuelRule;

    protected AirControlArea(){
        // for ORM only
    }

    public AirControlArea(final String code, final AirControlAreaBoundary boundaries, final String name, final double minimumReserveFuelRule) {
        Preconditions.nonNull(boundaries, "The boundaries must be provided.");
        Preconditions.nonEmpty(name, "The name of the Air Control Area cannot be null or empty.");
        Preconditions.ensure(minimumReserveFuelRule >= 0, "The minimum Reserve Fuel cannot be lower than 0.");

        this.acID = new AirControlAreaID(code);
        this.airControlAreaBoundaries = boundaries;
        this.name = name;
        this.minimumReserveFuelRule = minimumReserveFuelRule;
    }

    public String getName(){
        return this.name;
    }

    public AirControlAreaBoundary getAirControlAreaBoundaries() {
        return airControlAreaBoundaries;
    }

    public double getMinimumReserveFuelRule(){
        return this.minimumReserveFuelRule;
    }

    public void changeName(String name){
        Preconditions.nonEmpty(name, "The name of the Air Control Area cannot be null or empty.");
        this.name = name;
    }

    public void changeMinimumReserveFuelRule(double mrfr){
        Preconditions.ensure(mrfr >= 0, "The minimum Reserve Fuel cannot be lower than 0.");
        this.minimumReserveFuelRule = mrfr;
    }

    public void setNewAirControlAreaBoundaries(AirControlAreaBoundary acab){
        Preconditions.nonNull(acab, "The boundaries must be provided.");
        this.airControlAreaBoundaries = acab;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AirControlArea)) {
            return false;
        }
        final AirControlArea that = (AirControlArea) other;
        if (this == that) {
            return true;
        }
        return identity().equals(that.identity());
    }

    @Override
    public AirControlAreaID identity() {
        return this.acID;
    }
}