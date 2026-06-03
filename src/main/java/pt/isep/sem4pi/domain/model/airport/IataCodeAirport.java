package pt.isep.sem4pi.domain.model.airport;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

@Embeddable
public class IataCodeAirport implements ValueObject, Comparable<IataCodeAirport> {
    private String iataCode;

    protected IataCodeAirport() {
    }

    public IataCodeAirport(String iataCode) {
        if (iataCode == null || !iataCode.matches("^[A-Za-z]{3}$")) {
            throw new IllegalArgumentException("IATA code must be exactly 3 letters.");
        }
        this.iataCode = iataCode.toUpperCase();
    }

    public String code() {
        return iataCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IataCodeAirport that)) return false;
        return iataCode.equals(that.iataCode);
    }

    @Override
    public int hashCode() {
        return iataCode.hashCode();
    }

    @Override
    public String toString() {
        return iataCode;
    }

    @Override
    public int compareTo(IataCodeAirport other) {
        return this.iataCode.compareTo(other.iataCode);
    }
}