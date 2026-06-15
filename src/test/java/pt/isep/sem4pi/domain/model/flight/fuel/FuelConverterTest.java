package pt.isep.sem4pi.domain.model.flight.fuel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FuelConverterTest {

    @Test
    public void ensureLitersAreConvertedToKilogramsUsingDensity() {
        assertEquals(3376.8, FuelConverter.toKilograms(4200.0, FuelUnit.L), 0.0001);
    }

    @Test
    public void ensureKilogramsRemainUnchanged() {
        assertEquals(4200.0, FuelConverter.toKilograms(4200.0, FuelUnit.KG), 0.0001);
    }
}
