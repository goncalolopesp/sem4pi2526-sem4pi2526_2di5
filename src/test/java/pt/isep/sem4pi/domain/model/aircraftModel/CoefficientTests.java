package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Test class for the Coefficient value object.
 * <p>This class contains unit tests for the Coefficient class,
 * verifying constructor validation for drag and lift coefficients,
 * including boundary checks, and equality and hashing behavior.</p>
 */
public class CoefficientTests {

    @Test
    public void testValidCoefficient() {
        BigDecimal drag = new BigDecimal("0.5");
        BigDecimal lift = new BigDecimal("2.0");

        Coefficient coefficient = new Coefficient(drag, lift);

        Coefficient expectedCoefficient = new Coefficient(drag, lift);

        assertEquals(expectedCoefficient, coefficient);

        assertEquals("Drag: 0.5, Lift: 2.0", coefficient.toString());
    }

    @Test
    public void testNullDragCoefficient() {
        try {
            new Coefficient(null, new BigDecimal("2.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNullLiftCoefficient() {
        try {
            new Coefficient(new BigDecimal("0.5"), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testZeroDragCoefficient() {
        try {
            new Coefficient(BigDecimal.ZERO, new BigDecimal("2.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNegativeDragCoefficient() {
        try {
            new Coefficient(new BigDecimal("-0.5"), new BigDecimal("2.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDragCoefficientTooHigh() {
        try {
            new Coefficient(new BigDecimal("1.5"), new BigDecimal("2.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testZeroLiftCoefficient() {
        try {
            new Coefficient(new BigDecimal("0.5"), BigDecimal.ZERO);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNegativeLiftCoefficient() {
        try {
            new Coefficient(new BigDecimal("0.5"), new BigDecimal("-2.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testLiftCoefficientTooHigh() {
        try {
            new Coefficient(new BigDecimal("0.5"), new BigDecimal("5.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testBoundaryDragCoefficient() {
        try {
            new Coefficient(new BigDecimal("1.0"), new BigDecimal("1.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testBoundaryLiftCoefficient() {
        try {
            new Coefficient(new BigDecimal("0.5"), new BigDecimal("4.0"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEquality() {
        Coefficient coef1 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));
        Coefficient coef2 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));

        assertEquals(coef1, coef2);
    }

    @Test
    public void testInequality() {
        Coefficient coef1 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));
        Coefficient coef2 = new Coefficient(new BigDecimal("0.6"), new BigDecimal("2.0"));

        assertNotEquals(coef1, coef2);
    }

    @Test
    public void testHashCodeConsistency() {
        Coefficient coef1 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));
        Coefficient coef2 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));

        assertEquals(coef1.hashCode(), coef2.hashCode());
    }
}
