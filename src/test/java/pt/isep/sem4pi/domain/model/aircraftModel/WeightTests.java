package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Weight value object.
 * <p>This class contains unit tests for the Weight class,
 * verifying constructor validation for empty weight, maximum zero fuel weight,
 * and maximum takeoff weight, including relationship constraints,
 * and equality and hashing behavior.</p>
 */
@DisplayName("Weight Tests")
public class WeightTests {

    @Test
    @DisplayName("Should create weight with valid values")
    void testValidWeight() {
        BigDecimal ew = new BigDecimal("50000");
        BigDecimal mzfw = new BigDecimal("60000");
        BigDecimal mtow = new BigDecimal("70000");

        Weight weight = new Weight(ew, mzfw, mtow);

        assertEquals(ew, weight.emptyWeight());
        assertEquals(mzfw, weight.maximumZeroFuelWeight());
        assertEquals(mtow, weight.maximumTakeOffWeight());
    }

    @Test
    @DisplayName("Should throw exception when empty weight is null")
    void testNullEmptyWeight() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(null, new BigDecimal("60000"), new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when max zero fuel weight is null")
    void testNullMzfw() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), null, new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when max take off weight is null")
    void testNullMtow() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), null)
        );
    }

    @Test
    @DisplayName("Should throw exception when empty weight is zero")
    void testZeroEmptyWeight() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(BigDecimal.ZERO, new BigDecimal("60000"), new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when empty weight is negative")
    void testNegativeEmptyWeight() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("-50000"), new BigDecimal("60000"), new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when max zero fuel weight is zero")
    void testZeroMzfw() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), BigDecimal.ZERO, new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when max zero fuel weight is negative")
    void testNegativeMzfw() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), new BigDecimal("-60000"), new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when max take off weight is zero")
    void testZeroMtow() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Should throw exception when max take off weight is negative")
    void testNegativeMtow() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("-70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when MZFW is not greater than empty weight")
    void testMzfwNotGreaterThanEw() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), new BigDecimal("50000"), new BigDecimal("70000"))
        );
    }

    @Test
    @DisplayName("Should throw exception when MTOW is not greater than MZFW")
    void testMtowNotGreaterThanMzfw() {
        assertThrows(IllegalArgumentException.class, () ->
            new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("60000"))
        );
    }

    @Test
    @DisplayName("Should accept boundary values where MZFW > EW and MTOW > MZFW")
    void testBoundaryValues() {
        BigDecimal ew = new BigDecimal("50000");
        BigDecimal mzfw = new BigDecimal("50000.01");
        BigDecimal mtow = new BigDecimal("50000.02");

        assertDoesNotThrow(() ->
            new Weight(ew, mzfw, mtow)
        );
    }

    @Test
    @DisplayName("Should verify equality between two weights")
    void testEquality() {
        Weight weight1 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));
        Weight weight2 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));

        assertEquals(weight1, weight2);
    }

    @Test
    @DisplayName("Should verify inequality between different weights")
    void testInequality() {
        Weight weight1 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));
        Weight weight2 = new Weight(new BigDecimal("50001"), new BigDecimal("60000"), new BigDecimal("70000"));

        assertNotEquals(weight1, weight2);
    }

    @Test
    @DisplayName("Should have consistent hashcode for equal weights")
    void testHashCodeConsistency() {
        Weight weight1 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));
        Weight weight2 = new Weight(new BigDecimal("50000"), new BigDecimal("60000"), new BigDecimal("70000"));

        assertEquals(weight1.hashCode(), weight2.hashCode());
    }
}
