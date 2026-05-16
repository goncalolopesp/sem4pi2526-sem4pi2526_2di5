package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

/**
 * Test class for the AircraftPerformance value object.
 * <p>This class contains unit tests for the AircraftPerformance class,
 * verifying constructor validation for service ceiling, cruise speed, and maximum range,
 * as well as equality and hashing behavior.</p>
 */
public class AircraftPerformanceTests {

    @Test
    public void testConstructorValidValues() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        AircraftPerformance ap = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        AircraftPerformance expectedAp = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);

        assertEquals(expectedAp, ap);
        assertEquals("serviceCeiling: 35000.00, cruiseSpeed: 500.00, maximumRange: 5000.00", ap.toString());
    }

    @Test
    public void testConstructorNullServiceCeiling() {
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        try {
            new AircraftPerformance(null, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorZeroServiceCeiling() {
        BigDecimal serviceCeiling = BigDecimal.ZERO;
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNegativeServiceCeiling() {
        BigDecimal serviceCeiling = new BigDecimal("-1000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullCruiseSpeed() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        try {
            new AircraftPerformance(serviceCeiling, null, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorZeroCruiseSpeed() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = BigDecimal.ZERO;
        BigDecimal maximumRange = new BigDecimal("5000.00");
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNegativeCruiseSpeed() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("-50.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNullMaximumRange() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorZeroMaximumRange() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = BigDecimal.ZERO;
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNegativeMaximumRange() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("-100.00");
        try {
            new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEqualsSameObject() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        AircraftPerformance ap = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        assertEquals(ap, ap);
    }

    @Test
    public void testEqualsDifferentValues() {
        BigDecimal serviceCeiling1 = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed1 = new BigDecimal("500.00");
        BigDecimal maximumRange1 = new BigDecimal("5000.00");
        AircraftPerformance ap1 = new AircraftPerformance(serviceCeiling1, cruiseSpeed1, maximumRange1);

        BigDecimal serviceCeiling2 = new BigDecimal("40000.00");
        BigDecimal cruiseSpeed2 = new BigDecimal("550.00");
        BigDecimal maximumRange2 = new BigDecimal("6000.00");
        AircraftPerformance ap2 = new AircraftPerformance(serviceCeiling2, cruiseSpeed2, maximumRange2);

        assertNotEquals(ap1, ap2);
    }

    @Test
    public void testEqualsSameValues() {
        BigDecimal serviceCeiling1 = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed1 = new BigDecimal("500.00");
        BigDecimal maximumRange1 = new BigDecimal("5000.00");
        AircraftPerformance ap1 = new AircraftPerformance(serviceCeiling1, cruiseSpeed1, maximumRange1);

        BigDecimal serviceCeiling2 = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed2 = new BigDecimal("500.00");
        BigDecimal maximumRange2 = new BigDecimal("5000.00");
        AircraftPerformance ap2 = new AircraftPerformance(serviceCeiling2, cruiseSpeed2, maximumRange2);

        assertEquals(ap1, ap2);
    }

    @Test
    public void testEqualsDifferentScaleSameValue() {
        // serviceCeiling: 35000 vs 35000.00, cruiseSpeed: 500 vs 500.00, maximumRange: 5000 vs 5000.00
        BigDecimal serviceCeiling1 = new BigDecimal("35000");
        BigDecimal cruiseSpeed1 = new BigDecimal("500");
        BigDecimal maximumRange1 = new BigDecimal("5000");
        AircraftPerformance ap1 = new AircraftPerformance(serviceCeiling1, cruiseSpeed1, maximumRange1);

        BigDecimal serviceCeiling2 = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed2 = new BigDecimal("500.00");
        BigDecimal maximumRange2 = new BigDecimal("5000.00");
        AircraftPerformance ap2 = new AircraftPerformance(serviceCeiling2, cruiseSpeed2, maximumRange2);

        assertEquals(ap1, ap2);
        assertEquals(ap1.hashCode(), ap2.hashCode());
    }

    @Test
    public void testHashCodeConsistency() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        AircraftPerformance ap1 = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        AircraftPerformance ap2 = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);

        assertEquals(ap1.hashCode(), ap2.hashCode());
    }
}

