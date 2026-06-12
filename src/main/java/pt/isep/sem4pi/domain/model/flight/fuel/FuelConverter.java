package pt.isep.sem4pi.domain.model.flight.fuel;

public final class FuelConverter {

    /** Jet A-1 density used in project specifications (kg per liter). */
    public static final double JET_A1_DENSITY_KG_PER_LITER = 0.804;

    private FuelConverter() {
        // utility
    }

    public static double toKilograms(final double quantity, final FuelUnit unit) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Fuel quantity must be strictly positive.");
        }
        if (unit == FuelUnit.KG) {
            return quantity;
        }
        if (unit == FuelUnit.L) {
            return quantity * JET_A1_DENSITY_KG_PER_LITER;
        }
        throw new IllegalArgumentException("Unsupported fuel unit for mass conversion: " + unit);
    }

    public static double toKilograms(final Fuel fuel) {
        if (fuel == null || fuel.capacity() == null || fuel.unit() == null) {
            throw new IllegalArgumentException("Fuel information is incomplete.");
        }
        return toKilograms(fuel.capacity().value(), fuel.unit());
    }
}
