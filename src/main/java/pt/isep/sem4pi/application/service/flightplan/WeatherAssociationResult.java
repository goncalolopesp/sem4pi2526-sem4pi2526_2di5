package pt.isep.sem4pi.application.service.flightplan;

import pt.isep.sem4pi.domain.model.flight.FlightPlan;

/**
 * Resultado da US082 contendo o aggregate atualizado e informacao de apresentacao sobre a operacao.
 *
 * <p>{@code previousTestInvalidated} indica a UI que deve avisar que as
 * alteracoes meteorologicas tornaram obsoleto um resultado de validacao
 * anterior. {@code associatedWeatherCount} exclui selecoes duplicadas e
 * registos ja existentes no plano.</p>
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
