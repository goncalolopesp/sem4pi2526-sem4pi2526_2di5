package pt.isep.sem4pi.domain.model.airport;

import org.junit.Test;

import static org.junit.Assert.*;

public class IataCodeCompanyAirportTest {

    @Test
    public void ensureValidThreeLetterIataCodeIsCreated() {
        IataCodeAirport iata = new IataCodeAirport("OPO");
        assertEquals("OPO", iata.code());
    }

    @Test
    public void ensureIataCodeConvertsToLowercaseToUppercase() {
        IataCodeAirport iata = new IataCodeAirport("opo");
        assertEquals("OPO", iata.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfNull() {
        new IataCodeAirport(null);
    }

    @Test
    public void ensureIataCodeFailsIfTwoLetters() {
        try {
            new IataCodeAirport("TP");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("IATA code must be exactly 3 letters.", e.getMessage());
        }
    }

    @Test
    public void ensureIataCodeFailsIfFourLetters() {
        try {
            new IataCodeAirport("OPOX");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("IATA code must be exactly 3 letters.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfContainsNumbers() {
        new IataCodeAirport("OP1");
    }
}