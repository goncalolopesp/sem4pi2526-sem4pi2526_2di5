package pt.isep.sem4pi.domain.model.airport;

import org.junit.Test;
import pt.isep.sem4pi.domain.airport.IcaoCodeAirport;

import static org.junit.Assert.*;

public class IcaoCodeCompanyAirportTest {

    @Test
    public void ensureValidFourLetterIcaoCodeIsCreated() {
        IcaoCodeAirport icao = new IcaoCodeAirport("LPPR");
        assertEquals("LPPR", icao.code());
    }

    @Test
    public void ensureIcaoCodeConvertsToLowercaseToUppercase() {
        IcaoCodeAirport icao = new IcaoCodeAirport("lppr");
        assertEquals("LPPR", icao.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfNull() {
        new IcaoCodeAirport(null);
    }

    @Test
    public void ensureIcaoCodeFailsIfThreeLetters() {
        try {
            new IcaoCodeAirport("LPP");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("ICAO code must be 4 letters.", e.getMessage());
        }
    }

    @Test
    public void ensureIcaoCodeFailsIfFiveLetters() {
        try {
            new IcaoCodeAirport("LPPRX");
            fail("Expected an IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("ICAO code must be 4 letters.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfContainsNumbers() {
        new IcaoCodeAirport("LPP1");
    }
}