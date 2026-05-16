package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Test class for the Weight value object.
 * <p>This class contains unit tests for the Weight class,
 * verifying constructor validation for empty weight, maximum zero fuel weight,
 * and maximum takeoff weight, including relationship constraints,
 * and equality and hashing behavior.</p>
 */
public class WeightTests {

    @Test
    public void testValidWeight() {
        BigDecimal ew = new BigDecimal("50000");
        BigDecimal mzfw = new BigDecimal("60000");
        BigDecimal mtow = new BigDecimal("70000");

        Weight weight = new Weight(ew, mzfw, mtow);
        Weight expectedWeight = new Weight(ew, mzfw, mtow);

        assertEquals(expectedWeight, weight);
        assertEquals("Empty Weight: 50000, Max Zero Fuel Weight: 60000, Max Takeoff Weight: 70000", weight.toString());
    }

    @Test
    public void testNullEmptyWeight() {
        try {
            new Weight(null, new BigDecimal("60000"), new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNullMzfw() {
        try {
            new Weight(new BigDecimal("50000"), null, new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNullMtow() {
        try {
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testZeroEmptyWeight() {
        try {
            new Weight(BigDecimal.ZERO, new BigDecimal("60000"), new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNegativeEmptyWeight() {
        try {
            new Weight(new BigDecimal("-50000"), new BigDecimal("60000"), new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testZeroMzfw() {
        try {
            new Weight(new BigDecimal("50000"), BigDecimal.ZERO, new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNegativeMzfw() {
        try {
            new Weight(new BigDecimal("50000"), new BigDecimal("-60000"), new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testZeroMtow() {
        try {
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), BigDecimal.ZERO);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNegativeMtow() {
        try {
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("-70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testMzfwNotGreaterThanEw() {
        try {
            new Weight(new BigDecimal("50000"), new BigDecimal("50000"), new BigDecimal("70000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testMtowNotGreaterThanMzfw() {
        try {
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("60000"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testBoundaryValues() {
        BigDecimal ew = new BigDecimal("50000");
        BigDecimal mzfw = new BigDecimal("50000.01");
        BigDecimal mtow = new BigDecimal("50000.02");

        try {
            new Weight(ew, mzfw, mtow);
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage());
        }
    }

    @Test
    public void testEquality() {
        Weight weight1 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));
        Weight weight2 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));

        assertEquals(weight1, weight2);
    }

    @Test
    public void testInequality() {
        Weight weight1 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));
        Weight weight2 = new Weight(new BigDecimal("50001"), new BigDecimal("60000"), new BigDecimal("70000"));

        assertNotEquals(weight1, weight2);
    }

    @Test
    public void testHashCodeConsistency() {
        Weight weight1 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));
        Weight weight2 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));

        assertEquals(weight1.hashCode(), weight2.hashCode());
    }
}
