package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EngineModelId implements ValueObject, Comparable<EngineModelId>, Serializable{

    private String code;

    public EngineModelId() {}

    public EngineModelId(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("The Engine Model code cannot be null or empty!");
        }
        this.code = code;
    }

    @Override
    public int compareTo(EngineModelId o) {
        return this.code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EngineModelId that = (EngineModelId) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return this.code;
    }




}
