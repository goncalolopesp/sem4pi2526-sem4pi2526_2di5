package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

/**
 * Test class for the Fuel value object.
 * <p>This class contains unit tests for the Fuel class,
 * verifying constructor validation for maximum fuel capacity,
 * and equality and hashing behavior.</p>
 */
public class FuelTests {

    @Test
    public void testConstructorValidValues() {
        BigDecimal maxFuelCapacity = new BigDecimal("100.00");
        Fuel fuel = new Fuel(maxFuelCapacity);
        Fuel expected = new Fuel(maxFuelCapacity);

        assertEquals(expected, fuel);
        assertEquals("Fuel{maxFuelCapacity=100.00}", fuel.toString());
    }

    @Test
    public void testConstructorNullMaxFuelCapacity() {
        try {
            new Fuel(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorZeroMaxFuelCapacity() {
        BigDecimal maxFuelCapacity = BigDecimal.ZERO;
        try {
            new Fuel(maxFuelCapacity);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorNegativeMaxFuelCapacity() {
        BigDecimal maxFuelCapacity = new BigDecimal("-10.00");
        try {
            new Fuel(maxFuelCapacity);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEqualsSameObject() {
        BigDecimal maxFuelCapacity = new BigDecimal("100.00");
        Fuel fuel = new Fuel(maxFuelCapacity);
        assertEquals(fuel, fuel);
    }

    @Test
    public void testEqualsDifferentValues() {
        BigDecimal maxFuelCapacity1 = new BigDecimal("100.00");
        Fuel fuel1 = new Fuel(maxFuelCapacity1);

        BigDecimal maxFuelCapacity2 = new BigDecimal("200.00");
        Fuel fuel2 = new Fuel(maxFuelCapacity2);

        assertNotEquals(fuel1, fuel2);
    }

    @Test
    public void testEqualsSameValues() {
        BigDecimal maxFuelCapacity1 = new BigDecimal("100.00");
        Fuel fuel1 = new Fuel(maxFuelCapacity1);

        BigDecimal maxFuelCapacity2 = new BigDecimal("100.00");
        Fuel fuel2 = new Fuel(maxFuelCapacity2);

        assertEquals(fuel1, fuel2);
    }

    @Test
    public void testEqualsDifferentScaleSameValue() {
        // maxFuelCapacity: 100 vs 100.00
        BigDecimal maxFuelCapacity1 = new BigDecimal("100");
        Fuel fuel1 = new Fuel(maxFuelCapacity1);

        BigDecimal maxFuelCapacity2 = new BigDecimal("100.00");
        Fuel fuel2 = new Fuel(maxFuelCapacity2);

        assertEquals(fuel1, fuel2);
        assertEquals(fuel1.hashCode(), fuel2.hashCode());
    }

    @Test
    public void testHashCodeConsistency() {
        BigDecimal maxFuelCapacity = new BigDecimal("100.00");
        Fuel fuel1 = new Fuel(maxFuelCapacity);
        Fuel fuel2 = new Fuel(maxFuelCapacity);

        assertEquals(fuel1.hashCode(), fuel2.hashCode());
    }
}
