package pt.isep.sem4pi.domain.aircontrolarea;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AirControlAreaTest {

    private AirControlAreaBoundary validBoundary;

    @Before
    public void setUp() {
        validBoundary = new AirControlAreaBoundary(36.0, 42.0, -9.5, -6.0);
    }

    private AirControlArea validArea() {
        return new AirControlArea("LPPC", validBoundary, "Lisboa FIR", 2000.0);
    }

    @Test
    public void ensureValidAreaIsCreated() {
        AirControlArea area = validArea();
        assertEquals("LPPC", area.identity().getCode());
        assertEquals("Lisboa FIR", area.getName());
        assertEquals(2000.0, area.getMinimumReserveFuelRule(), 0.0001);
        assertNotNull(area.getAirControlAreaBoundaries());
    }

    @Test
    public void ensureZeroMinFuelIsAccepted() {
        AirControlArea area = new AirControlArea("LPPC", validBoundary, "Lisboa FIR", 0.0);
        assertEquals(0.0, area.getMinimumReserveFuelRule(), 0.0001);
    }

    @Test(expected = Exception.class)
    public void ensureNullBoundariesThrows() {
        new AirControlArea("LPPC", null, "Lisboa FIR", 2000.0);
    }

    @Test(expected = Exception.class)
    public void ensureNullNameThrows() {
        new AirControlArea("LPPC", validBoundary, null, 2000.0);
    }

    @Test(expected = Exception.class)
    public void ensureEmptyNameThrows() {
        new AirControlArea("LPPC", validBoundary, "", 2000.0);
    }

    @Test(expected = Exception.class)
    public void ensureBlankNameThrows() {
        new AirControlArea("LPPC", validBoundary, "   ", 2000.0);
    }

    @Test(expected = Exception.class)
    public void ensureNegativeMinFuelThrows() {
        new AirControlArea("LPPC", validBoundary, "Lisboa FIR", -1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullCodeThrows() {
        new AirControlArea(null, validBoundary, "Lisboa FIR", 2000.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureEmptyCodeThrows() {
        new AirControlArea("", validBoundary, "Lisboa FIR", 2000.0);
    }

    @Test
    public void ensureChangeNameUpdatesName() {
        AirControlArea area = validArea();
        area.changeName("Porto FIR");
        assertEquals("Porto FIR", area.getName());
    }

    @Test(expected = Exception.class)
    public void ensureChangeNameWithNullThrows() {
        validArea().changeName(null);
    }

    @Test(expected = Exception.class)
    public void ensureChangeNameWithEmptyThrows() {
        validArea().changeName("");
    }

    @Test(expected = Exception.class)
    public void ensureChangeNameWithBlankThrows() {
        validArea().changeName("   ");
    }

    @Test
    public void ensureChangeFuelRuleUpdatesValue() {
        AirControlArea area = validArea();
        area.changeMinimumReserveFuelRule(3500.0);
        assertEquals(3500.0, area.getMinimumReserveFuelRule(), 0.0001);
    }

    @Test
    public void ensureChangeFuelRuleToZeroIsAccepted() {
        AirControlArea area = validArea();
        area.changeMinimumReserveFuelRule(0.0);
        assertEquals(0.0, area.getMinimumReserveFuelRule(), 0.0001);
    }

    @Test(expected = Exception.class)
    public void ensureChangeFuelRuleToNegativeThrows() {
        validArea().changeMinimumReserveFuelRule(-0.1);
    }

    @Test
    public void ensureSetNewBoundariesUpdatesBoundaries() {
        AirControlArea area = validArea();
        AirControlAreaBoundary newBoundary = new AirControlAreaBoundary(40.0, 50.0, -5.0, 0.0);
        area.setNewAirControlAreaBoundaries(newBoundary);
        assertEquals(40.0, area.getAirControlAreaBoundaries().getMinLatitude(), 0.0001);
    }

    @Test(expected = Exception.class)
    public void ensureSetNewBoundariesWithNullThrows() {
        validArea().setNewAirControlAreaBoundaries(null);
    }

    @Test
    public void ensureIdentityReturnsCorrectID() {
        AirControlArea area = validArea();
        assertEquals(new AirControlAreaID("LPPC"), area.identity());
    }

    @Test
    public void ensureSameAsReturnsTrueForSameCode() {
        AirControlArea a = new AirControlArea("LPPC", validBoundary, "Lisboa FIR", 2000.0);
        AirControlArea b = new AirControlArea("LPPC", validBoundary, "Outro Nome", 500.0);
        assertTrue(a.sameAs(b));
    }

    @Test
    public void ensureSameAsReturnsFalseForDifferentCode() {
        AirControlArea a = new AirControlArea("LPPC", validBoundary, "Lisboa FIR", 2000.0);
        AirControlArea b = new AirControlArea("EDGG", validBoundary, "Langen FIR", 2000.0);
        assertFalse(a.sameAs(b));
    }

    @Test
    public void ensureSameAsReturnsFalseForNonAirControlArea() {
        AirControlArea area = validArea();
        assertFalse(area.sameAs("some string"));
    }

    @Test
    public void ensureSameAsReturnsTrueForSameInstance() {
        AirControlArea area = validArea();
        assertTrue(area.sameAs(area));
    }
}