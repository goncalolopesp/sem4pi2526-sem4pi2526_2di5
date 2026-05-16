package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Coefficient value object.
 * <p>This class contains unit tests for the Coefficient class,
 * verifying constructor validation for drag and lift coefficients,
 * including boundary checks, and equality and hashing behavior.</p>
 */
@DisplayName("Coefficient Tests")
public class CoefficientTests {

    @Test
    @DisplayName("Should create coefficient with valid values")
    void testValidCoefficient() {
        BigDecimal drag = new BigDecimal("0.5");
        BigDecimal lift = new BigDecimal("2.0");

        Coefficient coefficient = new Coefficient(drag, lift);

        Coefficient expectedCoefficient = new Coefficient(drag, lift);

        assertEquals(expectedCoefficient, coefficient);

        assertEquals("Drag: 0.5, Lift: 2.0", coefficient.toString());
    }

    @Test
    @DisplayName("Should throw exception when drag coefficient is null")
    void testNullDragCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(null, new BigDecimal("2.0"))
        );
    }

    @Test
    @DisplayName("Should throw exception when lift coefficient is null")
    void testNullLiftCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(new BigDecimal("0.5"), null)
        );
    }

    @Test
    @DisplayName("Should throw exception when drag coefficient is zero")
    void testZeroDragCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(BigDecimal.ZERO, new BigDecimal("2.0"))
        );
    }

    @Test
    @DisplayName("Should throw exception when drag coefficient is negative")
    void testNegativeDragCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(new BigDecimal("-0.5"), new BigDecimal("2.0"))
        );
    }

    @Test
    @DisplayName("Should throw exception when drag coefficient exceeds 1.0")
    void testDragCoefficientTooHigh() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(new BigDecimal("1.5"), new BigDecimal("2.0"))
        );
    }

    @Test
    @DisplayName("Should throw exception when lift coefficient is zero")
    void testZeroLiftCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(new BigDecimal("0.5"), BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Should throw exception when lift coefficient is negative")
    void testNegativeLiftCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(new BigDecimal("0.5"), new BigDecimal("-2.0"))
        );
    }

    @Test
    @DisplayName("Should throw exception when lift coefficient exceeds 4.0")
    void testLiftCoefficientTooHigh() {
        assertThrows(IllegalArgumentException.class, () ->
            new Coefficient(new BigDecimal("0.5"), new BigDecimal("5.0"))
        );
    }

    @Test
    @DisplayName("Should reject drag coefficient value 1.0")
    void testBoundaryDragCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coefficient(new BigDecimal("1.0"), new BigDecimal("1.0"))
        );
    }

    @Test
    @DisplayName("Should not accept boundary lift coefficient value 4.0")
    void testBoundaryLiftCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coefficient(new BigDecimal("0.5"), new BigDecimal("4.0"))
        );
    }

    @Test
    @DisplayName("Should verify equality between two coefficients")
    void testEquality() {
        Coefficient coef1 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));
        Coefficient coef2 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));

        assertEquals(coef1, coef2);
    }

    @Test
    @DisplayName("Should verify inequality between different coefficients")
    void testInequality() {
        Coefficient coef1 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));
        Coefficient coef2 = new Coefficient(new BigDecimal("0.6"), new BigDecimal("2.0"));

        assertNotEquals(coef1, coef2);
    }

    @Test
    @DisplayName("Should have consistent hashcode for equal coefficients")
    void testHashCodeConsistency() {
        Coefficient coef1 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));
        Coefficient coef2 = new Coefficient(new BigDecimal("0.5"), new BigDecimal("2.0"));

        assertEquals(coef1.hashCode(), coef2.hashCode());
    }
}
