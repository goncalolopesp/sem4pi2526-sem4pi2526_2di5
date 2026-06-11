package pt.isep.sem4pi.infrastructure.simulation;

import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlightPlanJsonExporter {

    private static final double FEET_TO_METERS = 0.3048;
    private static final String OUTPUT_DIR = "scomp/data/generated";

    private final AirportRepository airportRepository;

    public FlightPlanJsonExporter(final AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Path export(final FlightPlan plan, final FlightRoute route) throws IOException {
        final Path outputDir = Path.of(OUTPUT_DIR);
        Files.createDirectories(outputDir);

        final Path output = outputDir.resolve(plan.identity().value() + ".json");
        Files.writeString(output, buildJson(plan, route));
        return output;
    }

    private String buildJson(final FlightPlan plan, final FlightRoute route) {
        if (plan.legs().isEmpty()) {
            throw new IllegalArgumentException("Flight plan has no legs.");
        }

        final FlightLeg leg = plan.legs().get(0);

        final Airport origin = airport(route.originAirport());
        final Airport destination = airport(leg.arrival().airport());

        final double cruiseAltitudeM = cruiseAltitudeMeters(route);
        final List<Coordinates> path = buildPath(route, origin, destination);

        final Coordinates climbEnd = interpolate(path, 0.25);
        final Coordinates cruiseEnd = interpolate(path, 0.75);

        final double fuelKg = leg.fuel().capacity().value();
        final String departureTime = String.format(
                Locale.ROOT,
                "%02d:%02d",
                leg.departure().time().hour(),
                leg.departure().time().minute()
        );

        final StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"ID\": \"").append(plan.identity().value()).append("\",\n");
        json.append("  \"Type\": \"").append(plan.flightType().name().toLowerCase(Locale.ROOT)).append("\",\n");
        json.append("  \"Route\": \"").append(route.identity().code()).append("\",\n");
        json.append("  \"DepartureTime\": \"").append(departureTime).append("\",\n");
        json.append("  \"Leg\": [{\n");
        json.append("    \"Departure\": \"").append(origin.identity().code()).append("\",\n");
        json.append("    \"Arrival\": \"").append(destination.identity().code()).append("\",\n");
        json.append("    \"Fuel\": { \"Quantity\": ").append(fuelKg).append(", \"Unit\": \"kg\" },\n");
        json.append("    \"Flight Profile\": {\n");
        json.append("      \"Climb\": { \"Speed\": { \"Value\": 210, \"Unit\": \"Knots\" }, \"RateClimb\": { \"Value\": 8.0, \"Unit\": \"m/s\" } },\n");
        json.append("      \"Cruise\": { \"Speed\": { \"Value\": 460, \"Unit\": \"Knots\" } },\n");
        json.append("      \"Descend\": { \"Speed\": { \"Value\": 180, \"Unit\": \"Knots\" }, \"Ratedescend\": { \"Value\": -7.0, \"Unit\": \"m/s\" } }\n");
        json.append("    },\n");
        json.append("    \"Segments\": [\n");
        appendSegment(json, "climb", origin.getCoordinates(), origin.getElevation().getMeters(),
                climbEnd, cruiseAltitudeM, true);
        json.append(",\n");
        appendSegment(json, "cruise", climbEnd, cruiseAltitudeM, cruiseEnd, cruiseAltitudeM, false);
        json.append(",\n");
        appendSegment(json, "descend", cruiseEnd, cruiseAltitudeM,
                destination.getCoordinates(), destination.getElevation().getMeters(), true);
        json.append("\n    ]\n");
        json.append("  }]\n");
        json.append("}\n");
        return json.toString();
    }

    private Airport airport(final IataCodeAirport code) {
        return airportRepository.ofIdentity(code)
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + code.code()));
    }

    private double cruiseAltitudeMeters(final FlightRoute route) {
        if (route.segments().isEmpty()) {
            throw new IllegalArgumentException("Route has no segments.");
        }

        final RouteSegment segment = route.segments().get(0);
        if (segment.allowedAltitudes().isEmpty()) {
            throw new IllegalArgumentException("Route segment has no altitude slots.");
        }

        final AltitudeRange range = segment.allowedAltitudes().get(0);
        final double averageFeet = (range.minAltitude() + range.maxAltitude()) / 2.0;
        return averageFeet * FEET_TO_METERS;
    }

    private List<Coordinates> buildPath(final FlightRoute route, final Airport origin, final Airport destination) {
        final List<Coordinates> points = new ArrayList<>();
        points.add(origin.getCoordinates());
        for (final RouteSegment segment : route.segments()) {
            points.add(segment.endNode());
        }
        if (points.size() == 1) {
            points.add(destination.getCoordinates());
        }
        return points;
    }

    private Coordinates interpolate(final List<Coordinates> path, final double ratio) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Flight path has no coordinates.");
        }
        if (path.size() < 2) {
            return path.get(0);
        }

        final double total = path.size() - 1;
        final double scaled = ratio * total;
        final int index = Math.min((int) scaled, path.size() - 2);
        final double localRatio = scaled - index;

        final Coordinates start = path.get(index);
        final Coordinates end = path.get(index + 1);
        return new Coordinates(
                start.getLatitude() + (end.getLatitude() - start.getLatitude()) * localRatio,
                start.getLongitude() + (end.getLongitude() - start.getLongitude()) * localRatio
        );
    }

    private void appendSegment(final StringBuilder json,
                               final String mode,
                               final Coordinates start,
                               final double startAltitudeM,
                               final Coordinates end,
                               final double endAltitudeM,
                               final boolean trailingCommaAllowed) {
        json.append("      {\n");
        json.append("        \"Mode\": \"").append(mode).append("\",\n");
        json.append("        \"Start\": ").append(position(start, startAltitudeM)).append(",\n");
        json.append("        \"End\": ").append(position(end, endAltitudeM)).append("\n");
        json.append("      }");
    }

    private String position(final Coordinates coordinates, final double altitudeM) {
        return String.format(
                Locale.ROOT,
                "{ \"Latitude\": %.6f, \"Longitude\": %.6f, \"Altitude\": { \"Quantity\": %.1f, \"Unit\": \"m\" } }",
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                altitudeM
        );
    }
}
