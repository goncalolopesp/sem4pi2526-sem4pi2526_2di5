package pt.isep.sem4pi.domain.model.airport;

import org.junit.Test;
import static org.junit.Assert.*;

public class IcaoCodeTest {

    @Test
    public void ensureValidFourLetterIcaoCodeIsCreated() {
        IcaoCode icao = new IcaoCode("LPPR");
        assertEquals("LPPR", icao.code());
    }

    @Test
    public void ensureIcaoCodeConvertsToLowercaseToUppercase() {
        IcaoCode icao = new IcaoCode("lppr");
        assertEquals("LPPR", icao.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfNull() {
        new IcaoCode(null);
    }

    @Test
    public void ensureIcaoCodeFailsIfThreeLetters() {
        try {
            new IcaoCode("LPP");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Airport ICAO code must be exactly 4 letters.", e.getMessage());
        }
    }

    @Test
    public void ensureIcaoCodeFailsIfFiveLetters() {
        try {
            new IcaoCode("LPPRX");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Airport ICAO code must be exactly 4 letters.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfContainsNumbers() {
        new IcaoCode("LPP1");
    }
}