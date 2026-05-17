package pt.isep.sem4pi.domain.model.airport;

import jakarta.persistence.Embeddable;

@Embeddable
public class IcaoCode {
    private String icaoCode;

    protected IcaoCode() {
    }

    public IcaoCode(String icaoCode) {
        if (icaoCode == null || !icaoCode.matches("^[A-Za-z]{4}$")) {
            throw new IllegalArgumentException("ICAO code must be 4 letters.");
        }
        this.icaoCode = icaoCode.toUpperCase();
    }

    public String code() {
        return icaoCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IcaoCode that)) return false;
        return icaoCode.equals(that.icaoCode);
    }

    @Override
    public int hashCode() {
        return icaoCode.hashCode();
    }
}