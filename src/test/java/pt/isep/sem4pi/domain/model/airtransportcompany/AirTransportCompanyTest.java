package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;
import static org.junit.Assert.*;

public class AirTransportCompanyTest {

    @Test
    public void ensureValidCompanyIsCreated() {
        CompanyName name = new CompanyName("Ryanair");
        IataCode iata = new IataCode("FR");
        IcaoCode icao = new IcaoCode("RYR");

        AirTransportCompany company = new AirTransportCompany(name, iata, icao);

        assertEquals(iata, company.identityIata());
        assertEquals(icao, company.identityIcao());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyFailsIfNameIsNull() {
        new AirTransportCompany(null, new IataCode("FR"), new IcaoCode("RYR"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyFailsIfIataIsNull() {
        new AirTransportCompany(new CompanyName("Ryanair"), null, new IcaoCode("RYR"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyFailsIfIcaoIsNull() {
        new AirTransportCompany(new CompanyName("Ryanair"), new IataCode("FR"), null);
    }
}