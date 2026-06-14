package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.flight.FlightStatus;
import pt.isep.sem4pi.domain.model.reporting.ReportSection;
import pt.isep.sem4pi.domain.model.simulation.SimulationStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static pt.isep.sem4pi.domain.model.reporting.ReportSection.Entry.of;

public class MonthlyStatisticsReportBuilder {

    public MonthlyStatisticsReport build(final YearMonth period,
                                         final LocalDateTime generatedAt,
                                         final MonthlyStatisticsData data) {
        final List<ReportSection.Entry> simulationEntries = new ArrayList<>();
        simulationEntries.add(of("Simulations started", data.simulations()));
        for (final SimulationStatus status : SimulationStatus.values()) {
            simulationEntries.add(of("Status " + status,
                    data.simulationsByStatus().getOrDefault(status, 0L)));
        }
        simulationEntries.add(of("Flight inputs used", data.simulatedFlightInputs()));

        final List<ReportSection.Entry> flightEntries = new ArrayList<>();
        flightEntries.add(of("Scheduled flights", data.scheduledFlights()));
        for (final FlightStatus status : FlightStatus.values()) {
            flightEntries.add(of("Status " + status,
                    data.flightsByStatus().getOrDefault(status, 0L)));
        }

        final List<ReportSection.Entry> chartEntries = new ArrayList<>();
        chartEntries.add(of("Simulations", bar(data.simulations())));
        chartEntries.add(of("Simulation reports", bar(data.simulationReports())));
        chartEntries.add(of("Reported flights", bar(data.reportedFlights())));
        chartEntries.add(of("Safety violations", bar(data.safetyViolations())));
        chartEntries.add(of("Scheduled flights", bar(data.scheduledFlights())));

        return new MonthlyStatisticsReport(period, generatedAt, List.of(
                new ReportSection("Simulation Statistics", simulationEntries),
                ReportSection.of("Simulation Report Statistics",
                        of("Simulation reports generated", data.simulationReports()),
                        of("Flights represented in reports", data.reportedFlights())),
                ReportSection.of("Safety Statistics",
                        of("Safety violations reported", data.safetyViolations())),
                new ReportSection("Flight Data Statistics", flightEntries),
                new ReportSection("Monthly Overview Chart", chartEntries)
        ));
    }

    private String bar(final int value) {
        final int visibleUnits = Math.min(Math.max(value, 0), 40);
        return "#".repeat(visibleUnits) + " (" + value + ")";
    }
}
