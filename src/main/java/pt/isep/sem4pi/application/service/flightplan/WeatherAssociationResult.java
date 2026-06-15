package pt.isep.sem4pi.application.service.flightplan;

import pt.isep.sem4pi.domain.model.flight.FlightPlan;

/**
 * Result of US082 containing both the updated aggregate and presentation
 * information about the operation.
 *
 * <p>{@code previousTestInvalidated} tells the UI to warn that weather changes
 * made a previous validation result obsolete. {@code associatedWeatherCount}
 * excludes duplicate selections and records already present in the plan.</p>
 */
public class WeatherAssociationResult {

    private final FlightPlan flightPlan;
    private final boolean previousTestInvalidated;
    private final int associatedWeatherCount;

    public WeatherAssociationResult(FlightPlan flightPlan, boolean previousTestInvalidated) {
        this(flightPlan, previousTestInvalidated, 1);
    }

    public WeatherAssociationResult(FlightPlan flightPlan, boolean previousTestInvalidated,
                                    int associatedWeatherCount) {
        this.flightPlan = flightPlan;
        this.previousTestInvalidated = previousTestInvalidated;
        this.associatedWeatherCount = associatedWeatherCount;
    }

    public FlightPlan flightPlan() {
        return flightPlan;
    }

    public boolean previousTestInvalidated() {
        return previousTestInvalidated;
    }

    public int associatedWeatherCount() {
        return associatedWeatherCount;
    }
}
