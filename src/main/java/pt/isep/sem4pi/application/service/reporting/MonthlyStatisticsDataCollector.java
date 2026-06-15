package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightStatus;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.model.simulation.SimulationStatus;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;

import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MonthlyStatisticsDataCollector
        implements ReportDataCollector<MonthlyStatisticsData> {

    // DEFESA:
    // O coletor tem alta coesão: seleciona e agrega factos do mês, mas não os apresenta nem os grava.
    private final SimulationRepository simulations;
    private final SimulationReportRepository simulationReports;
    private final FlightRepository flights;
    private final SafetyViolationTotalReader violationTotalReader;

    public MonthlyStatisticsDataCollector(final SimulationRepository simulations,
                                          final SimulationReportRepository simulationReports,
                                          final FlightRepository flights,
                                          final SafetyViolationTotalReader violationTotalReader) {
        this.simulations = simulations;
        this.simulationReports = simulationReports;
        this.flights = flights;
        this.violationTotalReader = violationTotalReader;
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
                monthlyReports.stream()
                        .mapToInt(violationTotalReader::totalOf)
                        .sum(),
                scheduledFlights,
                flightsByStatus);
    }
}
