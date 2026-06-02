package pt.isep.sem4pi.domain.model.aircontrolarea;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class AirControlAreaID implements ValueObject, Comparable<AirControlAreaID>, Serializable {

    private String areaCode;

    protected AirControlAreaID(){
        // for ORM only
    }

    public AirControlAreaID(final String code){
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("The Air Control Area code cannot be null nor empty!");
        }
        this.areaCode = code;
    }

    public String getCode(){
        return this.areaCode;
    }

    @Override
    public int compareTo(final AirControlAreaID ac){
        return this.areaCode.compareTo(ac.areaCode);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AirControlAreaID)) {
            return false;
        }
        final AirControlAreaID that = (AirControlAreaID) o;
        return this.areaCode.equals(that.areaCode);
    }

    @Override
    public int hashCode() {
        return this.areaCode.hashCode();
    }

    @Override
    public String toString(){
        return this.areaCode;
    }
}