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

        return new MonthlyStatisticsReport(period, generatedAt, List.of(
                new ReportSection("Simulation Statistics", simulationEntries),
                ReportSection.of("Simulation Report Statistics",
                        of("Simulation reports generated", data.simulationReports()),
                        of("Flights represented in reports", data.reportedFlights())),
                ReportSection.of("Safety Statistics",
                        of("Safety violations reported", data.safetyViolations())),
                new ReportSection("Flight Data Statistics", flightEntries),
                ReportSection.of("Rendering Notice",
                        of("Graphics", "Not available in the console; textual summaries are used."))
        ));
    }
}
