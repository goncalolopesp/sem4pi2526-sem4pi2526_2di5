package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the WingGeometry value object.
 * <p>This class contains unit tests for the WingGeometry class,
 * verifying constructor validation for wing area and aspect ratio,
 * and equality and hashing behavior.</p>
 */
class WingGeometryTests {

    @Test
    void testConstructorValidValues() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        WingGeometry wg = new WingGeometry(wingArea, aspectRatio);
        assertEquals(wingArea, wg.wingArea());
        assertEquals(aspectRatio, wg.aspectRatio());
    }

    @Test
    void testConstructorNullWingArea() {
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        assertThrows(IllegalArgumentException.class, () -> new WingGeometry(null, aspectRatio));
    }

    @Test
    void testConstructorZeroWingArea() {
        BigDecimal wingArea = BigDecimal.ZERO;
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        assertThrows(IllegalArgumentException.class, () -> new WingGeometry(wingArea, aspectRatio));
    }

    @Test
    void testConstructorNegativeWingArea() {
        BigDecimal wingArea = new BigDecimal("-10.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        assertThrows(IllegalArgumentException.class, () -> new WingGeometry(wingArea, aspectRatio));
    }

    @Test
    void testConstructorNullAspectRatio() {
        BigDecimal wingArea = new BigDecimal("50.00");
        assertThrows(IllegalArgumentException.class, () -> new WingGeometry(wingArea, null));
    }

    @Test
    void testConstructorZeroAspectRatio() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = BigDecimal.ZERO;
        assertThrows(IllegalArgumentException.class, () -> new WingGeometry(wingArea, aspectRatio));
    }

    @Test
    void testConstructorNegativeAspectRatio() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("-1.0000");
        assertThrows(IllegalArgumentException.class, () -> new WingGeometry(wingArea, aspectRatio));
    }

    @Test
    void testEqualsSameObject() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        WingGeometry wg = new WingGeometry(wingArea, aspectRatio);
        assertEquals(wg, wg);
    }

    @Test
    void testEqualsDifferentValues() {
        BigDecimal wingArea1 = new BigDecimal("50.00");
        BigDecimal aspectRatio1 = new BigDecimal("7.5000");
        WingGeometry wg1 = new WingGeometry(wingArea1, aspectRatio1);

        BigDecimal wingArea2 = new BigDecimal("60.00");
        BigDecimal aspectRatio2 = new BigDecimal("8.0000");
        WingGeometry wg2 = new WingGeometry(wingArea2, aspectRatio2);

        assertNotEquals(wg1, wg2);
    }

    @Test
    void testEqualsSameValues() {
        BigDecimal wingArea1 = new BigDecimal("50.00");
        BigDecimal aspectRatio1 = new BigDecimal("7.5000");
        WingGeometry wg1 = new WingGeometry(wingArea1, aspectRatio1);

        BigDecimal wingArea2 = new BigDecimal("50.00");
        BigDecimal aspectRatio2 = new BigDecimal("7.5000");
        WingGeometry wg2 = new WingGeometry(wingArea2, aspectRatio2);

        assertEquals(wg1, wg2);
    }

    @Test
    void testEqualsDifferentScaleSameValue() {
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
    void testHashCodeConsistency() {
        BigDecimal wingArea = new BigDecimal("50.00");
        BigDecimal aspectRatio = new BigDecimal("7.5000");
        WingGeometry wg1 = new WingGeometry(wingArea, aspectRatio);
        WingGeometry wg2 = new WingGeometry(wingArea, aspectRatio);

        assertEquals(wg1.hashCode(), wg2.hashCode());
    }
}

