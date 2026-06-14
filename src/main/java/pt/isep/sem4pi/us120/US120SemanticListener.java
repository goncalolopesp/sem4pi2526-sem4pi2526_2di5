package pt.isep.sem4pi.us120;

import pt.isep.sem4pi.us120.US120Parser.ArrivalContext;
import pt.isep.sem4pi.us120.US120Parser.DepartureContext;
import pt.isep.sem4pi.us120.US120Parser.FlightContext;
import pt.isep.sem4pi.us120.US120Parser.LegContext;
import pt.isep.sem4pi.us120.US120Parser.RouteContext;
import pt.isep.sem4pi.us120.US120Parser.SegmentContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class US120SemanticListener extends US120BaseListener {

    private final List<String> errors = new ArrayList<>();

    private RouteContext currentRoute;
    private final List<LegState> legs = new ArrayList<>();
    private LegState currentLeg;
    private double[] previousSegmentEnd;

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public void enterFlight(final FlightContext ctx) {
        currentRoute = ctx.route();
        legs.clear();
        previousSegmentEnd = null;
    }

    @Override
    public void enterLeg(final LegContext ctx) {
        currentLeg = new LegState(ctx.getStart().getLine());
        previousSegmentEnd = null;
    }

    @Override
    public void exitSegment(final SegmentContext ctx) {
        if (currentLeg == null) {
            return;
        }

        final double[] start = US120SemanticSupport.parseCoord(ctx.beginNode().coord());
        final double[] end = US120SemanticSupport.parseCoord(ctx.endNode().coord());
        final int line = ctx.getStart().getLine();

        if (!US120SemanticSupport.differentCoordinates(start, end)) {
            addError(line, "Segment start and end coordinates must be different.");
        }

        if (previousSegmentEnd != null && !US120SemanticSupport.sameCoordinates(previousSegmentEnd, start)) {
            addError(line, "Segment start must match the previous segment end coordinates.");
        }

        previousSegmentEnd = end;
        currentLeg.segmentStarts.add(start);
        currentLeg.segmentEnds.add(end);
    }

    @Override
    public void exitLeg(final LegContext ctx) {
        if (currentLeg == null) {
            return;
        }

        final DepartureContext departure = ctx.departure();
        final ArrivalContext arrival = ctx.arrival();
        final int line = ctx.getStart().getLine();

        currentLeg.departureAirport = departure.airport().getText();
        currentLeg.arrivalAirport = arrival.airport().getText();

        if (!US120SemanticSupport.isValidCalendarDate(departure.date())
                || !US120SemanticSupport.isValidTime(departure.time())
                || !US120SemanticSupport.isValidTime(arrival.time())) {
            legs.add(currentLeg);
            currentLeg = null;
            previousSegmentEnd = null;
            return;
        }

        currentLeg.departureDate = US120SemanticSupport.parseDate(departure.date());
        currentLeg.departureTime = US120SemanticSupport.parseTime(departure.time());
        currentLeg.arrivalTime = US120SemanticSupport.parseTime(arrival.time());

        if (currentLeg.departureAirport.equalsIgnoreCase(currentLeg.arrivalAirport)) {
            addError(line, "Leg departure and arrival airports must be different.");
        }

        final LocalDateTime departureDateTime = LocalDateTime.of(currentLeg.departureDate, currentLeg.departureTime);
        final LocalDateTime arrivalDateTime = LocalDateTime.of(currentLeg.departureDate, currentLeg.arrivalTime);
        if (!arrivalDateTime.isAfter(departureDateTime)) {
            addError(line, "Leg arrival time must be after departure time.");
        }

        legs.add(currentLeg);
        currentLeg = null;
        previousSegmentEnd = null;
    }

    @Override
    public void exitFlight(final FlightContext ctx) {
        validateConsecutiveLegs(ctx.getStart().getLine());
        validateRepeatedAirports(ctx.getStart().getLine());
        validateRouteLegCoherence(ctx.getStart().getLine());
    }

    private void validateConsecutiveLegs(final int line) {
        for (int i = 1; i < legs.size(); i++) {
            final LegState previous = legs.get(i - 1);
            final LegState current = legs.get(i);

            if (!previous.arrivalAirport.equalsIgnoreCase(current.departureAirport)) {
                addError(line, "Leg " + (i + 1)
                        + " departure airport must match leg " + i + " arrival airport.");
            }

            if (!hasCompleteTemporalData(previous) || !hasCompleteTemporalData(current)) {
                continue;
            }

            final LocalDateTime previousArrival = LocalDateTime.of(previous.departureDate, previous.arrivalTime);
            final LocalDateTime currentDeparture = LocalDateTime.of(current.departureDate, current.departureTime);
            if (!currentDeparture.isAfter(previousArrival)) {
                addError(line, "Leg " + (i + 1)
                        + " departure must be temporally after leg " + i + " arrival.");
            }
        }
    }

    private static boolean hasCompleteTemporalData(final LegState leg) {
        return leg.departureDate != null && leg.departureTime != null && leg.arrivalTime != null;
    }

    private void validateRepeatedAirports(final int line) {
        final Set<String> visited = new HashSet<>();
        String previousArrival = null;

        for (int i = 0; i < legs.size(); i++) {
            final LegState leg = legs.get(i);
            final String departure = leg.departureAirport.toUpperCase();
            final String arrival = leg.arrivalAirport.toUpperCase();

            if (i == 0) {
                if (visited.contains(departure)) {
                    addError(line, "Repeated airport in flight itinerary: " + departure);
                }
                visited.add(departure);
            } else if (!departure.equalsIgnoreCase(previousArrival)) {
                addError(line, "Leg " + (i + 1) + " departure must continue from previous leg arrival.");
            }

            if (visited.contains(arrival) && !arrival.equalsIgnoreCase(departure)) {
                addError(line, "Repeated airport in flight itinerary: " + arrival);
            }
            visited.add(arrival);
            previousArrival = arrival;
        }
    }

    private void validateRouteLegCoherence(final int line) {
        if (currentRoute == null || legs.isEmpty()) {
            return;
        }

        final List<double[]> routeCoords = US120SemanticSupport.routeCoordinates(currentRoute);
        if (routeCoords.isEmpty()) {
            addError(line, "Route must declare at least one coordinate.");
            return;
        }

        final LegState firstLeg = legs.get(0);
        final LegState lastLeg = legs.get(legs.size() - 1);

        if (firstLeg.segmentStarts.isEmpty() || lastLeg.segmentEnds.isEmpty()) {
            addError(line, "Each leg must contain at least one segment to match the route.");
            return;
        }

        if (!US120SemanticSupport.sameCoordinates(routeCoords.get(0), firstLeg.segmentStarts.get(0))) {
            addError(line, "First leg segment must start at the first route coordinate.");
        }

        if (!US120SemanticSupport.sameCoordinates(
                routeCoords.get(routeCoords.size() - 1),
                lastLeg.segmentEnds.get(lastLeg.segmentEnds.size() - 1))) {
            addError(line, "Last leg segment must end at the last route coordinate.");
        }

        final List<double[]> itinerary = new ArrayList<>();
        for (final LegState leg : legs) {
            for (int i = 0; i < leg.segmentStarts.size(); i++) {
                itinerary.add(leg.segmentStarts.get(i));
                itinerary.add(leg.segmentEnds.get(i));
            }
        }

        int routeIndex = 0;
        int itineraryIndex = 0;
        while (routeIndex < routeCoords.size() && itineraryIndex < itinerary.size()) {
            if (US120SemanticSupport.sameCoordinates(routeCoords.get(routeIndex), itinerary.get(itineraryIndex))) {
                routeIndex++;
            }
            itineraryIndex++;
        }

        if (routeIndex < routeCoords.size()) {
            addError(line, "Route coordinates are not covered by leg segments.");
        }
    }

    private void addError(final int line, final String message) {
        errors.add(US120SemanticSupport.formatError(line, message));
    }

    private static final class LegState {
        private final int line;
        private String departureAirport;
        private String arrivalAirport;
        private LocalDate departureDate;
        private LocalTime departureTime;
        private LocalTime arrivalTime;
        private final List<double[]> segmentStarts = new ArrayList<>();
        private final List<double[]> segmentEnds = new ArrayList<>();

        private LegState(final int line) {
            this.line = line;
        }
    }
}
