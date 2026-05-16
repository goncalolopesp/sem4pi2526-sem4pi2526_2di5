package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;

import static org.junit.Assert.*;

public class IataCodeTest {

    @Test
    public void ensureValidIataCodeIsCreated() {
        IataCode code = new IataCode("TP");
        assertEquals("TP", code.code());
    }

    @Test
    public void ensureIataCodeConvertsToLowercaseToUppercase() {
        IataCode code = new IataCode("tp");
        assertEquals("TP", code.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfNull() {
        new IataCode(null);
    }

    @Test
    public void ensureIataCodeFailsIfThreeLetters() {
        try {
            new IataCode("TAP");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("IATA code must be exactly 2 letters.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfContainsNumbers() {
        new IataCode("T1");
    }
}