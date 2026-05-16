package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

/**
 * Test class for the WingGeometry value object.
 * <p>This class contains unit tests for the WingGeometry class,
 * verifying constructor validation for wing area and aspect ratio,
 * and equality and hashing behavior.</p>
 */
public class WingGeometryTests {

    @Test
    public void testConstructorValidValues() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        WingGeometry wg = new WingGeometry(wingArea, aspectRatio);
        WingGeometry expectedWg = new WingGeometry(wingArea, aspectRatio);

        assertEquals(expectedWg, wg);
        assertEquals("wingArea: 50.00, aspectRatio: 7.5000", wg.toString());
    }

    @Test
    public void testConstructorNullWingArea() {
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        try {
            new WingGeometry(null, aspectRatio);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorZeroWingArea() {
        BigDecimal wingArea = BigDecimal.ZERO;
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        try {
            new WingGeometry(wingArea, aspectRatio);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNegativeWingArea() {
        BigDecimal wingArea = new BigDecimal("-10.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        try {
            new WingGeometry(wingArea, aspectRatio);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullAspectRatio() {
        BigDecimal wingArea = new BigDecimal("50.00");
        try {
            new WingGeometry(wingArea, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorZeroAspectRatio() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = BigDecimal.ZERO;
        try {
            new WingGeometry(wingArea, aspectRatio);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNegativeAspectRatio() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("-1.0000");
        try {
            new WingGeometry(wingArea, aspectRatio);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEqualsSameObject() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        WingGeometry wg = new WingGeometry(wingArea, aspectRatio);
        assertEquals(wg, wg);
    }

    @Test
    public void testEqualsDifferentValues() {
        BigDecimal wingArea1 = new BigDecimal("50.00");
        BigDecimal aspectRatio1 = new BigDecimal("7.5000");
        WingGeometry wg1 = new WingGeometry(wingArea1, aspectRatio1);

        BigDecimal wingArea2 = new BigDecimal("60.00");
        BigDecimal aspectRatio2 = new BigDecimal("8.0000");
        WingGeometry wg2 = new WingGeometry(wingArea2, aspectRatio2);

        assertNotEquals(wg1, wg2);
    }

    @Test
    public void testEqualsSameValues() {
        BigDecimal wingArea1 = new BigDecimal("50.00");
        BigDecimal aspectRatio1 = new BigDecimal("7.5000");
        WingGeometry wg1 = new WingGeometry(wingArea1, aspectRatio1);

        BigDecimal wingArea2 = new BigDecimal("50.00");
        BigDecimal aspectRatio2 = new BigDecimal("7.5000");
        WingGeometry wg2 = new WingGeometry(wingArea2, aspectRatio2);

        assertEquals(wg1, wg2);
    }

    @Test
    public void testEqualsDifferentScaleSameValue() {
        // wingArea: 50 vs 50.00
        BigDecimal wingArea1 = new BigDecimal("50");
        BigDecimal aspectRatio1 = new BigDecimal("7.5");
        WingGeometry wg1 = new WingGeometry(wingArea1, aspectRatio1);

        BigDecimal wingArea2 = new BigDecimal("50.00");
        BigDecimal aspectRatio2 = new BigDecimal("7.5000");
        WingGeometry wg2 = new WingGeometry(wingArea2, aspectRatio2);

        assertEquals(wg1, wg2);
        assertEquals(wg1.hashCode(), wg2.hashCode());
    }

    @Test
    public void testHashCodeConsistency() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        WingGeometry wg1 = new WingGeometry(wingArea, aspectRatio);
        WingGeometry wg2 = new WingGeometry(wingArea, aspectRatio);

        assertEquals(wg1.hashCode(), wg2.hashCode());
    }
}

