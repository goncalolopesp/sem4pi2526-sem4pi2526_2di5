package pt.isep.sem4pi.domain.model.pilot;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class PilotId implements ValueObject, Comparable<PilotId>, Serializable {

    private String code;

    protected PilotId(){
        //for ORM
    }

    public PilotId(String code){
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Pilot id cannot be null or empty.");
        }
        this.code = code.trim();
    }

    public String code() {
        return code;
    }

    @Override
    public int compareTo(PilotId other){
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PilotId pilotId = (PilotId) o;
        return code.equals(pilotId.code);
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
