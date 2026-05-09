package pt.isep.sem4pi.domain.model.aircraftModel;

/**
 * Enumeration representing the different types of motorization/propulsion systems
 * that can be used in aircraft.
 *
 * <p>This enum defines the various engine types available for aircraft models,
 * which impact fuel consumption, performance, and operational characteristics.</p>
 */
public enum MotorizationType {
    TURBOPROP,
    TURBOFAN,
    TURBOJET,
    RAM_JET,
    ELECTRIC_PROPELLER,
    OTHER
}