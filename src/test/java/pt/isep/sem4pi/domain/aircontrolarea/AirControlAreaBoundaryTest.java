package pt.isep.sem4pi.domain.aircontrolarea;

import org.junit.Test;
import static org.junit.Assert.*;

public class AirControlAreaBoundaryTest {

    private AirControlAreaBoundary validBoundary() {
        return new AirControlAreaBoundary(36.0, 42.0, -9.5, -6.0);
    }

    @Test
    public void ensureValidBoundaryIsCreated() {
        AirControlAreaBoundary b = new AirControlAreaBoundary(-90.0, 90.0, -180.0, 180.0);
        assertEquals(-90.0, b.getMinLatitude(),  0.0001);
        assertEquals( 90.0, b.getMaxLatitude(),  0.0001);
        assertEquals(-180.0, b.getMinLongitude(), 0.0001);
        assertEquals( 180.0, b.getMaxLongitude(), 0.0001);
    }

    @Test
    public void ensureGettersReturnCorrectValues() {
        AirControlAreaBoundary b = new AirControlAreaBoundary(36.0, 42.0, -9.5, -6.0);
        assertEquals(36.0,  b.getMinLatitude(),  0.0001);
        assertEquals(42.0,  b.getMaxLatitude(),  0.0001);
        assertEquals(-9.5,  b.getMinLongitude(), 0.0001);
        assertEquals(-6.0,  b.getMaxLongitude(), 0.0001);
    }

    @Test(expected = Exception.class)
    public void ensureMinLatBelowNeg90Throws() {
        new AirControlAreaBoundary(-91.0, 42.0, -9.5, -6.0);
    }

    @Test(expected = Exception.class)
    public void ensureMaxLatAbove90Throws() {
        new AirControlAreaBoundary(36.0, 91.0, -9.5, -6.0);
    }

    @Test(expected = Exception.class)
    public void ensureMinLatEqualToMaxLatThrows() {
        new AirControlAreaBoundary(42.0, 42.0, -9.5, -6.0);
    }

    @Test(expected = Exception.class)
    public void ensureMinLatGreaterThanMaxLatThrows() {
        new AirControlAreaBoundary(45.0, 36.0, -9.5, -6.0);
    }

    @Test(expected = Exception.class)
    public void ensureMinLonBelowNeg180Throws() {
        new AirControlAreaBoundary(36.0, 42.0, -181.0, -6.0);
    }

    @Test(expected = Exception.class)
    public void ensureMaxLonAbove180Throws() {
        new AirControlAreaBoundary(36.0, 42.0, -9.5, 181.0);
    }

    @Test(expected = Exception.class)
    public void ensureMinLonEqualToMaxLonThrows() {
        new AirControlAreaBoundary(36.0, 42.0, -9.5, -9.5);
    }

    @Test(expected = Exception.class)
    public void ensureMinLonGreaterThanMaxLonThrows() {
        new AirControlAreaBoundary(36.0, 42.0, -6.0, -9.5);
    }

    @Test
    public void ensurePointInsideBoundaryIsIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertTrue(b.includes(39.0, -7.5));
    }

    @Test
    public void ensurePointOutsideLatIsNotIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertFalse(b.includes(50.0, -7.5));
    }

    @Test
    public void ensurePointOutsideLonIsNotIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertFalse(b.includes(39.0, 0.0));
    }

    @Test
    public void ensurePointOnMinLatBoundaryIsNotIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertFalse(b.includes(36.0, -7.5));
    }

    @Test
    public void ensurePointOnMaxLatBoundaryIsNotIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertFalse(b.includes(42.0, -7.5));
    }

    @Test
    public void ensurePointOnMinLonBoundaryIsNotIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertFalse(b.includes(39.0, -9.5));
    }

    @Test
    public void ensurePointOnMaxLonBoundaryIsNotIncluded() {
        AirControlAreaBoundary b = validBoundary();
        assertFalse(b.includes(39.0, -6.0));
    }

    @Test
    public void ensureMergeExpandsToUnion() {
        AirControlAreaBoundary a = new AirControlAreaBoundary(36.0, 42.0, -9.5, -6.0);
        AirControlAreaBoundary b = new AirControlAreaBoundary(40.0, 50.0, -12.0, -5.0);
        AirControlAreaBoundary merged = a.mergeWith(b);

        assertEquals(36.0,  merged.getMinLatitude(),  0.0001);
        assertEquals(50.0,  merged.getMaxLatitude(),  0.0001);
        assertEquals(-12.0, merged.getMinLongitude(), 0.0001);
        assertEquals(-5.0,  merged.getMaxLongitude(), 0.0001);
    }

    @Test
    public void ensureMergeWithIdenticalBoundaryReturnsSameBounds() {
        AirControlAreaBoundary a = validBoundary();
        AirControlAreaBoundary merged = a.mergeWith(a);

        assertEquals(a.getMinLatitude(),  merged.getMinLatitude(),  0.0001);
        assertEquals(a.getMaxLatitude(),  merged.getMaxLatitude(),  0.0001);
        assertEquals(a.getMinLongitude(), merged.getMinLongitude(), 0.0001);
        assertEquals(a.getMaxLongitude(), merged.getMaxLongitude(), 0.0001);
    }

    @Test
    public void ensureMergeIsCommutative() {
        AirControlAreaBoundary a = new AirControlAreaBoundary(36.0, 42.0, -9.5, -6.0);
        AirControlAreaBoundary b = new AirControlAreaBoundary(40.0, 50.0, -12.0, -5.0);

        AirControlAreaBoundary ab = a.mergeWith(b);
        AirControlAreaBoundary ba = b.mergeWith(a);

        assertEquals(ab.getMinLatitude(),  ba.getMinLatitude(),  0.0001);
        assertEquals(ab.getMaxLatitude(),  ba.getMaxLatitude(),  0.0001);
        assertEquals(ab.getMinLongitude(), ba.getMinLongitude(), 0.0001);
        assertEquals(ab.getMaxLongitude(), ba.getMaxLongitude(), 0.0001);
    }
}