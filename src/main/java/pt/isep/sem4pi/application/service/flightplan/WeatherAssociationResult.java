package pt.isep.sem4pi.application.service.flightplan;

import pt.isep.sem4pi.domain.model.flight.FlightPlan;

public class WeatherAssociationResult {

    private final FlightPlan flightPlan;
    private final boolean previousTestInvalidated;

    public WeatherAssociationResult(FlightPlan flightPlan, boolean previousTestInvalidated) {
        this.flightPlan = flightPlan;
        this.previousTestInvalidated = previousTestInvalidated;
    }

    public FlightPlan flightPlan() {
        return flightPlan;
    }

    public boolean previousTestInvalidated() {
        return previousTestInvalidated;
    }
}
