package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;

import static org.junit.Assert.*;

public class IcaoCodeCompanyAirportTest {

    @Test
    public void ensureValidTwoLetterIcaoCodeIsCreated() {
        IcaoCodeCompany code = new IcaoCodeCompany("TP");
        assertEquals("TP", code.code());
    }

    @Test
    public void ensureValidThreeLetterIcaoCodeIsCreated() {
        IcaoCodeCompany code = new IcaoCodeCompany("TAP");
        assertEquals("TAP", code.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfNull() {
        new IcaoCodeCompany(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfFourLetters() {
        new IcaoCodeCompany("TAPA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfContainsNumbers() {
        new IcaoCodeCompany("TA1");
    }
}