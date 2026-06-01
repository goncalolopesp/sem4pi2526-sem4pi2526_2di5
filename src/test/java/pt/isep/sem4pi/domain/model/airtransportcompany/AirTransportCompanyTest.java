package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.airtransportcompany.IcaoCodeCompany;

import static org.junit.Assert.assertEquals;

public class AirTransportCompanyTest {

    @Test
    public void ensureValidCompanyIsCreated() {
        CompanyName name = new CompanyName("Ryanair");
        IataCodeCompany iata = new IataCodeCompany("FR");
        IcaoCodeCompany icao = new IcaoCodeCompany("RYR");

        AirTransportCompany company = new AirTransportCompany(name, iata, icao);

        assertEquals(iata, company.identity());
        assertEquals(icao, company.getIcaoCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyFailsIfNameIsNull() {
        new AirTransportCompany(null, new IataCodeCompany("FR"), new IcaoCodeCompany("RYR"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyFailsIfIataIsNull() {
        new AirTransportCompany(new CompanyName("Ryanair"), null, new IcaoCodeCompany("RYR"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCompanyFailsIfIcaoIsNull() {
        new AirTransportCompany(new CompanyName("Ryanair"), new IataCodeCompany("FR"), null);
    }
}