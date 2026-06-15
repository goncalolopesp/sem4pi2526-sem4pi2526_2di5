package pt.isep.sem4pi.application.service.simulation;

import pt.isep.sem4pi.domain.model.simulation.FlightExecutionSummary;
import pt.isep.sem4pi.domain.model.simulation.SafetyViolationSummary;
import pt.isep.sem4pi.domain.model.simulation.SimulationValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimulationReportParser {

    // DEFESA:
    // Este parser é um Anti-Corruption Layer entre o formato textual do SCOMP e os objetos da aplicação Java.
    private static final Pattern FLIGHT_STATUS = Pattern.compile(
            "^\\s*\\[(.+?)]\\s+pid=.*?status=([A-Z]+).*$");
    private static final Pattern FLIGHT_HEADER = Pattern.compile(
            "^Flight\\s+([^,]+),.*$");
    private static final Pattern POSITION = Pattern.compile(
            "^\\s*t=(\\d+)\\s+lat=([-+\\d.]+)\\s+lon=([-+\\d.]+)\\s+alt=([-+\\d.]+).*$");
    private static final Pattern ALTITUDE_VIOLATION = Pattern.compile(
            "^\\s*t=(\\d+)\\s+ALTITUDE\\s+flight=(\\S+).*");
    private static final Pattern SEPARATION_VIOLATION = Pattern.compile(
            "^\\s*t=(\\d+)\\s+SEPARATION\\s+(\\S+)\\(pid=.*?\\)\\s+<->\\s+(\\S+)\\(pid=.*");
    private static final Pattern VIOLATION_SECTION_HEADER = Pattern.compile(
            "^=== US102 Safety Violations \\((\\d+) shown / (\\d+) total\\) ===$");

    public ParsedSimulationReport parse(final String reportPath) throws IOException {
        final List<String> lines = Files.readAllLines(Path.of(reportPath));
        final Map<String, Map<Integer, String>> positions = parsePositions(lines);
        final List<FlightExecutionSummary> flights = parseFlightStatuses(lines);
        final List<SafetyViolationSummary> violations =
                parseSafetyViolations(lines, positions);
        // DEFESA:
        // A validação rejeita relatórios incompletos para não persistir um resumo aparentemente consistente.
        validateViolationCount(lines, violations.size());

        final int declaredTotal = extractTotalFlights(lines);
        if (declaredTotal != flights.size()) {
            throw new IOException("SCOMP report flight count does not match flight statuses");
        }

        return new ParsedSimulationReport(
                flights,
                violations,
                SimulationValidationResult.from(extractFinalResult(lines)));
    }

    private Map<String, Map<Integer, String>> parsePositions(final List<String> lines) {
        final Map<String, Map<Integer, String>> positions = new HashMap<>();
        String currentFlight = null;

        for (final String line : lines) {
            final Matcher header = FLIGHT_HEADER.matcher(line);
            if (header.matches()) {
                currentFlight = header.group(1);
                positions.putIfAbsent(currentFlight, new HashMap<>());
                continue;
            }

            if (currentFlight != null) {
                final Matcher position = POSITION.matcher(line);
                if (position.matches()) {
                    positions.get(currentFlight).put(
                            Integer.parseInt(position.group(1)),
                            formatPosition(position));
                } else if (!line.isBlank() && !Character.isWhitespace(line.charAt(0))) {
                    currentFlight = null;
                }
            }
        }
        return positions;
    }

    private String formatPosition(final Matcher matcher) {
        return String.format("lat=%s, lon=%s, alt=%sm",
                matcher.group(2), matcher.group(3), matcher.group(4));
    }

    private List<FlightExecutionSummary> parseFlightStatuses(final List<String> lines) {
        final List<FlightExecutionSummary> result = new ArrayList<>();
        for (final String line : lines) {
            final Matcher matcher = FLIGHT_STATUS.matcher(line);
            if (matcher.matches()) {
                result.add(new FlightExecutionSummary(
                        matcher.group(1), matcher.group(2)));
            }
        }
        return result;
    }

    private void validateViolationCount(final List<String> lines,
                                        final int parsedViolationCount) throws IOException {
        for (final String line : lines) {
            final Matcher header = VIOLATION_SECTION_HEADER.matcher(line);
            if (!header.matches()) {
                continue;
            }

            final int shown = Integer.parseInt(header.group(1));
            final int total = Integer.parseInt(header.group(2));
            if (shown != parsedViolationCount) {
                throw new IOException(
                        "SCOMP report violation count does not match listed violations");
            }
            if (shown != total) {
                throw new IOException(
                        "SCOMP report omitted safety violation details");
            }
            return;
        }
        throw new IOException("SCOMP report does not contain safety violation totals");
    }

    private List<SafetyViolationSummary> parseSafetyViolations(
            final List<String> lines,
            final Map<String, Map<Integer, String>> positions) {
        final List<SafetyViolationSummary> result = new ArrayList<>();
        boolean inViolationSection = false;

        for (final String line : lines) {
            if (line.startsWith("=== US102 Safety Violations")) {
                inViolationSection = true;
                continue;
            }
            if (inViolationSection && line.startsWith("===")) {
                break;
            }
            if (!inViolationSection) {
                continue;
            }

            final Matcher altitude = ALTITUDE_VIOLATION.matcher(line);
            if (altitude.matches()) {
                final int timestamp = Integer.parseInt(altitude.group(1));
                final String flightId = altitude.group(2);
                result.add(new SafetyViolationSummary(
                        timestamp,
                        "ALTITUDE",
                        flightId + ": " + positionAt(positions, flightId, timestamp),
                        line.trim()));
                continue;
            }

            final Matcher separation = SEPARATION_VIOLATION.matcher(line);
            if (separation.matches()) {
                final int timestamp = Integer.parseInt(separation.group(1));
                final String firstFlight = separation.group(2);
                final String secondFlight = separation.group(3);
                result.add(new SafetyViolationSummary(
                        timestamp,
                        "SEPARATION",
                        firstFlight + ": " + positionAt(positions, firstFlight, timestamp)
                                + "; " + secondFlight + ": "
                                + positionAt(positions, secondFlight, timestamp),
                        line.trim()));
            }
        }
        return result;
    }

    private String positionAt(final Map<String, Map<Integer, String>> positions,
                              final String flightId,
                              final int timestamp) {
        return positions.getOrDefault(flightId, Map.of())
                .getOrDefault(timestamp, "position unavailable");
    }

    private int extractTotalFlights(final List<String> lines) throws IOException {
        for (final String line : lines) {
            if (line.startsWith("Total flights:")) {
                return Integer.parseInt(line.substring("Total flights:".length()).trim());
            }
        }
        throw new IOException("SCOMP report does not contain total flights");
    }

    private String extractFinalResult(final List<String> lines) throws IOException {
        for (final String line : lines) {
            if (line.startsWith("Simulation result:")) {
                return line.substring("Simulation result:".length()).trim();
            }
        }
        throw new IOException("SCOMP report does not contain a validation result");
    }
}
