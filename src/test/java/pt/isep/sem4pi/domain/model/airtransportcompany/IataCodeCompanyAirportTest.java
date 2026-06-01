package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;
import pt.isep.sem4pi.domain.airtransportcompany.IataCodeCompany;

import static org.junit.Assert.*;

public class IataCodeCompanyAirportTest {

    @Test
    public void ensureValidIataCodeIsCreated() {
        IataCodeCompany code = new IataCodeCompany("TP");
        assertEquals("TP", code.code());
    }

    @Test
    public void ensureIataCodeConvertsToLowercaseToUppercase() {
        IataCodeCompany code = new IataCodeCompany("tp");
        assertEquals("TP", code.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfNull() {
        new IataCodeCompany(null);
    }

    @Test
    public void ensureIataCodeFailsIfThreeLetters() {
        try {
            new IataCodeCompany("TAP");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("IATA code must be exactly 2 letters.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfContainsNumbers() {
        new IataCodeCompany("T1");
    }
}