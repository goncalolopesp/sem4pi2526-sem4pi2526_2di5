package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.flight.FlightStatus;
import pt.isep.sem4pi.domain.model.simulation.SimulationStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record MonthlyStatisticsData(
        int simulations,
        Map<SimulationStatus, Long> simulationsByStatus,
        int simulatedFlightInputs,
        int simulationReports,
        int reportedFlights,
        int safetyViolations,
        int scheduledFlights,
        Map<FlightStatus, Long> flightsByStatus) {

    public MonthlyStatisticsData {
        simulationsByStatus = immutableEnumMap(SimulationStatus.class, simulationsByStatus);
        flightsByStatus = immutableEnumMap(FlightStatus.class, flightsByStatus);
    }

    private static <E extends Enum<E>> Map<E, Long> immutableEnumMap(
            final Class<E> enumType, final Map<E, Long> values) {
        final EnumMap<E, Long> copy = new EnumMap<>(enumType);
        copy.putAll(values);
        return Collections.unmodifiableMap(copy);
    }
}
