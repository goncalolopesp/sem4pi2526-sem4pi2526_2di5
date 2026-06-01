package pt.isep.sem4pi.domain.airtransportcompany;

import jakarta.persistence.Embeddable;

@Embeddable
public class IcaoCodeCompany {
    private String icaoCode;

    protected IcaoCodeCompany() {
    }

    public IcaoCodeCompany(String icaoCode) {
        if (icaoCode == null || !icaoCode.matches("^[A-Za-z]{2,3}$")) {
            throw new IllegalArgumentException("ICAO code must be 2 or 3 letters.");
        }
        this.icaoCode = icaoCode.toUpperCase();
    }

    public String code() {
        return icaoCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IcaoCodeCompany that)) return false;
        return icaoCode.equals(that.icaoCode);
    }

    @Override
    public int hashCode() {
        return icaoCode.hashCode();
    }
}