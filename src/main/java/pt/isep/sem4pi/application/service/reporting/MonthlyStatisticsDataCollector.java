package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightStatus;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationStatus;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonthlyStatisticsDataCollector
        implements ReportDataCollector<MonthlyStatisticsData> {

    private static final Pattern VIOLATION_TOTAL = Pattern.compile(
            "Safety Violations \\(\\d+ shown / (\\d+) total\\)",
            Pattern.CASE_INSENSITIVE);

    private final SimulationRepository simulations;
    private final SimulationReportRepository simulationReports;
    private final FlightRepository flights;

    public MonthlyStatisticsDataCollector(final SimulationRepository simulations,
                                          final SimulationReportRepository simulationReports,
                                          final FlightRepository flights) {
        this.simulations = simulations;
        this.simulationReports = simulationReports;
        this.flights = flights;
    }

    @Override
    public MonthlyStatisticsData collect(final YearMonth period) {
        final List<Simulation> monthlySimulations = simulations.findAll().stream()
                .filter(simulation -> YearMonth.from(simulation.startTime()).equals(period))
                .toList();
        final Map<SimulationStatus, Long> simulationsByStatus =
                new EnumMap<>(SimulationStatus.class);
        for (final SimulationStatus status : SimulationStatus.values()) {
            simulationsByStatus.put(status, monthlySimulations.stream()
                    .filter(simulation -> simulation.status() == status)
                    .count());
        }

        final List<SimulationReport> monthlyReports = simulationReports.findAll().stream()
                .filter(report -> YearMonth.from(report.generatedAt()).equals(period))
                .toList();

        int scheduledFlights = 0;
        final Map<FlightStatus, Long> flightsByStatus = new EnumMap<>(FlightStatus.class);
        for (final FlightStatus status : FlightStatus.values()) {
            flightsByStatus.put(status, 0L);
        }
        for (final Flight flight : flights.findAll()) {
            if (YearMonth.from(flight.departureDate()).equals(period)) {
                scheduledFlights++;
                flightsByStatus.merge(flight.status(), 1L, Long::sum);
            }
        }

        return new MonthlyStatisticsData(
                monthlySimulations.size(),
                simulationsByStatus,
                monthlySimulations.stream()
                        .mapToInt(simulation -> simulation.simulatedFlightInputs().size())
                        .sum(),
                monthlyReports.size(),
                monthlyReports.stream().mapToInt(SimulationReport::totalFlights).sum(),
                monthlyReports.stream().mapToInt(this::readViolationTotal).sum(),
                scheduledFlights,
                flightsByStatus);
    }

    private int readViolationTotal(final SimulationReport report) {
        try {
            final String content = Files.readString(Path.of(report.fileLocation()));
            final Matcher matcher = VIOLATION_TOTAL.matcher(content);
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
        } catch (final IOException | RuntimeException ignored) {
            return 0;
        }
    }
}
