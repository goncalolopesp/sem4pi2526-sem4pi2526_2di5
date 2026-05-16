package pt.isep.sem4pi.domain.model.airport;

import org.junit.Test;
import static org.junit.Assert.*;

public class IataCodeTest {

    @Test
    public void ensureValidThreeLetterIataCodeIsCreated() {
        IataCode iata = new IataCode("OPO");
        assertEquals("OPO", iata.code());
    }

    @Test
    public void ensureIataCodeConvertsToLowercaseToUppercase() {
        IataCode iata = new IataCode("opo");
        assertEquals("OPO", iata.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfNull() {
        new IataCode(null);
    }

    @Test
    public void ensureIataCodeFailsIfTwoLetters() {
        try {
            new IataCode("TP");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Airport IATA code must be exactly 3 letters.", e.getMessage());
        }
    }

    @Test
    public void ensureIataCodeFailsIfFourLetters() {
        try {
            new IataCode("OPOX");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Airport IATA code must be exactly 3 letters.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIataCodeFailsIfContainsNumbers() {
        new IataCode("OP1");
    }
}