package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class FuelTests {

    @Test
    void testConstructorValidValues() {
        BigDecimal maxFuelCapacity = new BigDecimal("100.00");
        Fuel fuel = new Fuel(maxFuelCapacity);
        assertEquals(maxFuelCapacity, fuel.maxFuelCapacity());
    }

    @Test
    void testConstructorNullMaxFuelCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new Fuel(null));
    }

    @Test
    void testConstructorZeroMaxFuelCapacity() {
        BigDecimal maxFuelCapacity = BigDecimal.ZERO;
        assertThrows(IllegalArgumentException.class, () -> new Fuel(maxFuelCapacity));
    }

    @Test
    void testConstructorNegativeMaxFuelCapacity() {
        BigDecimal maxFuelCapacity = new BigDecimal("-10.00");
        assertThrows(IllegalArgumentException.class, () -> new Fuel(maxFuelCapacity));
    }

    @Test
    void testEqualsSameObject() {
        BigDecimal maxFuelCapacity = new BigDecimal("100.00");
        Fuel fuel = new Fuel(maxFuelCapacity);
        assertEquals(fuel, fuel);
    }

    @Test
    void testEqualsDifferentValues() {
        BigDecimal maxFuelCapacity1 = new BigDecimal("100.00");
        Fuel fuel1 = new Fuel(maxFuelCapacity1);

        BigDecimal maxFuelCapacity2 = new BigDecimal("200.00");
        Fuel fuel2 = new Fuel(maxFuelCapacity2);

        assertNotEquals(fuel1, fuel2);
    }

    @Test
    void testEqualsSameValues() {
        BigDecimal maxFuelCapacity1 = new BigDecimal("100.00");
        Fuel fuel1 = new Fuel(maxFuelCapacity1);

        BigDecimal maxFuelCapacity2 = new BigDecimal("100.00");
        Fuel fuel2 = new Fuel(maxFuelCapacity2);

        assertEquals(fuel1, fuel2);
    }

    @Test
    void testEqualsDifferentScaleSameValue() {
        // maxFuelCapacity: 100 vs 100.00
        BigDecimal maxFuelCapacity1 = new BigDecimal("100");
        Fuel fuel1 = new Fuel(maxFuelCapacity1);

        BigDecimal maxFuelCapacity2 = new BigDecimal("100.00");
        Fuel fuel2 = new Fuel(maxFuelCapacity2);

        assertEquals(fuel1, fuel2);
        assertEquals(fuel1.hashCode(), fuel2.hashCode());
    }

    @Test
    void testHashCodeConsistency() {
        BigDecimal maxFuelCapacity = new BigDecimal("100.00");
        Fuel fuel1 = new Fuel(maxFuelCapacity);
        Fuel fuel2 = new Fuel(maxFuelCapacity);

        assertEquals(fuel1.hashCode(), fuel2.hashCode());
    }
}

