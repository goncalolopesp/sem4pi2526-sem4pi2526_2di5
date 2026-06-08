package pt.isep.sem4pi.domain.model.flight;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FlightPlanId implements ValueObject, Comparable<FlightPlanId>, Serializable {

    private String code;

    protected FlightPlanId(){
        //for ORM
    }

    public FlightPlanId(String code){
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight plan id cannot be null or empty.");
        }
        this.code = code.trim();
    }

    public String value() {
        return code;
    }

    @Override
    public int compareTo(FlightPlanId other){
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightPlanId that = (FlightPlanId) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
