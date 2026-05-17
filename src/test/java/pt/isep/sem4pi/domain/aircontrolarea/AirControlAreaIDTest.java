package pt.isep.sem4pi.domain.aircontrolarea;

import org.junit.Test;
import static org.junit.Assert.*;

public class AirControlAreaIDTest {

    @Test
    public void ensureValidCodeIsAccepted() {
        AirControlAreaID id = new AirControlAreaID("LPPC");
        assertEquals("LPPC", id.getCode());
    }

    @Test
    public void ensureCodeWithSpacesAndLettersIsAccepted() {
        AirControlAreaID id = new AirControlAreaID("  AREA-01  ");
        assertEquals("  AREA-01  ", id.getCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullCodeThrows() {
        new AirControlAreaID(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureEmptyCodeThrows() {
        new AirControlAreaID("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBlankCodeThrows() {
        new AirControlAreaID("   ");
    }

    @Test
    public void ensureSameCodeIsEqual() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        AirControlAreaID b = new AirControlAreaID("LPPC");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void ensureDifferentCodesAreNotEqual() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        AirControlAreaID b = new AirControlAreaID("EDGG");
        assertNotEquals(a, b);
    }

    @Test
    public void ensureNotEqualToNull() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        assertNotEquals(a, null);
    }

    @Test
    public void ensureNotEqualToDifferentType() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        assertNotEquals(a, "LPPC");
    }

    @Test
    public void ensureEqualToItself() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        assertEquals(a, a);
    }

    @Test
    public void ensureCompareToReturnsZeroForEqualCodes() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        AirControlAreaID b = new AirControlAreaID("LPPC");
        assertEquals(0, a.compareTo(b));
    }

    @Test
    public void ensureCompareToReturnsNegativeWhenLesser() {
        AirControlAreaID a = new AirControlAreaID("EDGG");
        AirControlAreaID b = new AirControlAreaID("LPPC");
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void ensureCompareToReturnsPositiveWhenGreater() {
        AirControlAreaID a = new AirControlAreaID("LPPC");
        AirControlAreaID b = new AirControlAreaID("EDGG");
        assertTrue(a.compareTo(b) > 0);
    }

    @Test
    public void ensureToStringReturnsCode() {
        AirControlAreaID id = new AirControlAreaID("LPPC");
        assertEquals("LPPC", id.toString());
    }
}