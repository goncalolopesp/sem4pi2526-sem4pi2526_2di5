package pt.isep.sem4pi.domain.model.shared;

import jakarta.persistence.Embeddable;

@Embeddable
public class IataCode {
    private String iataCode;

    protected IataCode() {
    }

    public IataCode(String iataCode) {
        if (iataCode == null || !iataCode.matches("^[A-Za-z]{2}$")) {
            throw new IllegalArgumentException("IATA code must be exactly 2 letters.");
        }
        this.iataCode = iataCode.toUpperCase();
    }

    public String code() {
        return iataCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IataCode that)) return false;
        return iataCode.equals(that.iataCode);
    }

    @Override
    public int hashCode() {
        return iataCode.hashCode();
    }
}