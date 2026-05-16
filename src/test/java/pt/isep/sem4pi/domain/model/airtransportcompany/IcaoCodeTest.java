package pt.isep.sem4pi.domain.model.airtransportcompany;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.shared.IcaoCode;

import static org.junit.Assert.*;

public class IcaoCodeTest {

    @Test
    public void ensureValidTwoLetterIcaoCodeIsCreated() {
        IcaoCode code = new IcaoCode("TP");
        assertEquals("TP", code.code());
    }

    @Test
    public void ensureValidThreeLetterIcaoCodeIsCreated() {
        IcaoCode code = new IcaoCode("TAP");
        assertEquals("TAP", code.code());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfNull() {
        new IcaoCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfFourLetters() {
        new IcaoCode("TAPA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIcaoCodeFailsIfContainsNumbers() {
        new IcaoCode("TA1");
    }
}