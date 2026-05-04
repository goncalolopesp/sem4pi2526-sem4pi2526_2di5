package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AircraftPerformanceTests {

    @Test
    void testConstructorValidValues() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        AircraftPerformance ap = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        assertEquals(serviceCeiling, ap.serviceCeiling());
        assertEquals(cruiseSpeed, ap.cruiseSpeed());
        assertEquals(maximumRange, ap.maximumRange());
    }

    @Test
    void testConstructorNullServiceCeiling() {
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(null, cruiseSpeed, maximumRange));
    }

    @Test
    void testConstructorZeroServiceCeiling() {
        BigDecimal serviceCeiling = BigDecimal.ZERO;
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange));
    }

    @Test
    void testConstructorNegativeServiceCeiling() {
        BigDecimal serviceCeiling = new BigDecimal("-1000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange));
    }

    @Test
    void testConstructorNullCruiseSpeed() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, null, maximumRange));
    }

    @Test
    void testConstructorZeroCruiseSpeed() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = BigDecimal.ZERO;
        BigDecimal maximumRange = new BigDecimal("5000.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange));
    }

    @Test
    void testConstructorNegativeCruiseSpeed() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("-50.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange));
    }

    @Test
    void testConstructorNullMaximumRange() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, null));
    }

    @Test
    void testConstructorZeroMaximumRange() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = BigDecimal.ZERO;
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange));
    }

    @Test
    void testConstructorNegativeMaximumRange() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("-100.00");
        assertThrows(IllegalArgumentException.class, () -> new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange));
    }

    @Test
    void testEqualsSameObject() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        AircraftPerformance ap = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        assertEquals(ap, ap);
    }

    @Test
    void testEqualsDifferentValues() {
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
    void testEqualsSameValues() {
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
    void testEqualsDifferentScaleSameValue() {
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
    void testHashCodeConsistency() {
        BigDecimal serviceCeiling = new BigDecimal("35000.00");
        BigDecimal cruiseSpeed = new BigDecimal("500.00");
        BigDecimal maximumRange = new BigDecimal("5000.00");
        AircraftPerformance ap1 = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        AircraftPerformance ap2 = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);

        assertEquals(ap1.hashCode(), ap2.hashCode());
    }
}

