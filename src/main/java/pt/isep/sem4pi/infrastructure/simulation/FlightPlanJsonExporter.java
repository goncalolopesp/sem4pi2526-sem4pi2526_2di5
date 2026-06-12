package pt.isep.sem4pi.infrastructure.simulation;

import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanAltitudeSlotDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanSegmentDTO;
import pt.isep.sem4pi.domain.model.flight.FlightLeg;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelConverter;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class FlightPlanJsonExporter {

    private static final double FEET_TO_METERS = 0.3048;
    private static final String OUTPUT_DIR = "scomp/data/generated";

    private final AirportRepository airportRepository;

    public FlightPlanJsonExporter(final AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Path export(final FlightPlan plan, final CreateFlightPlanDTO dslPlan) throws IOException {
        if (dslPlan == null || dslPlan.getLegs() == null || dslPlan.getLegs().isEmpty()) {
            throw new IllegalArgumentException("DSL flight plan data is required for faithful export.");
        }

        final Path outputDir = Path.of(OUTPUT_DIR);
        Files.createDirectories(outputDir);

        final Path output = outputDir.resolve(plan.identity().value() + ".json");
        Files.writeString(output, buildJson(plan, dslPlan));
        return output;
    }

    private String buildJson(final FlightPlan plan, final CreateFlightPlanDTO dslPlan) {
        final CreateFlightPlanLegDTO firstLeg = dslPlan.getLegs().get(0);
        final StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"ID\": \"").append(plan.identity().value()).append("\",\n");
        json.append("  \"Type\": \"").append(plan.flightType().name().toLowerCase(Locale.ROOT)).append("\",\n");
        json.append("  \"Route\": \"").append(dslPlan.getRouteId()).append("\",\n");
        json.append("  \"DepartureTime\": \"").append(formatTime(
                firstLeg.getDepartureHour(),
                firstLeg.getDepartureMinute())).append("\",\n");
        json.append("  \"Leg\": [\n");

        for (int legIndex = 0; legIndex < dslPlan.getLegs().size(); legIndex++) {
            appendLeg(json, dslPlan.getLegs().get(legIndex), legIndex < dslPlan.getLegs().size() - 1);
        }

        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private void appendLeg(final StringBuilder json,
                           final CreateFlightPlanLegDTO legDto,
                           final boolean trailingComma) {
        final Airport departure = airport(new IataCodeAirport(legDto.getDepartureAirportIata()));
        final Airport arrival = airport(new IataCodeAirport(legDto.getArrivalAirportIata()));
        final double fuelKg = FuelConverter.toKilograms(
                legDto.getFuelQuantity(),
                FuelUnit.valueOf(legDto.getFuelUnit()));

        json.append("    {\n");
        json.append("      \"Departure\": \"").append(departure.identity().code()).append("\",\n");
        json.append("      \"Arrival\": \"").append(arrival.identity().code()).append("\",\n");
        json.append("      \"Fuel\": { \"Quantity\": ")
                .append(String.format(Locale.ROOT, "%.1f", fuelKg))
                .append(", \"Unit\": \"kg\" },\n");

        final List<CreateFlightPlanSegmentDTO> segments = legDto.getSegments();
        final double cruiseSpeed = segments.isEmpty() ? 460.0 : segments.get(0).getSpeed();
        json.append("      \"Flight Profile\": {\n");
        json.append("        \"Climb\": { \"Speed\": { \"Value\": ")
                .append(formatSpeed(cruiseSpeed))
                .append(", \"Unit\": \"Knots\" }, \"RateClimb\": { \"Value\": 8.0, \"Unit\": \"m/s\" } },\n");
        json.append("        \"Cruise\": { \"Speed\": { \"Value\": ")
                .append(formatSpeed(cruiseSpeed))
                .append(", \"Unit\": \"Knots\" } },\n");
        json.append("        \"Descend\": { \"Speed\": { \"Value\": ")
                .append(formatSpeed(cruiseSpeed))
                .append(", \"Unit\": \"Knots\" }, \"Ratedescend\": { \"Value\": -7.0, \"Unit\": \"m/s\" } }\n");
        json.append("      },\n");
        json.append("      \"Segments\": [\n");

        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Leg must contain at least one DSL segment for export.");
        }

        for (int segmentIndex = 0; segmentIndex < segments.size(); segmentIndex++) {
            if (segments.size() == 1) {
                appendSingleDslSegmentAsProfile(
                        json,
                        segments.get(0),
                        departure,
                        arrival
                );
                break;
            }
            appendDslSegment(
                    json,
                    segments.get(segmentIndex),
                    departure,
                    arrival,
                    segmentIndex,
                    segments.size(),
                    segmentIndex < segments.size() - 1
            );
        }

        json.append("      ]\n");
        json.append("    }");
        if (trailingComma) {
            json.append(",");
        }
        json.append("\n");
    }

    private void appendSingleDslSegmentAsProfile(final StringBuilder json,
                                                 final CreateFlightPlanSegmentDTO segment,
                                                 final Airport departure,
                                                 final Airport arrival) {
        final double cruiseAltitudeM = averageAltitudeMeters(segment.getAltitudeSlots());
        final double departureElevationM = departure.getElevation().getMeters();
        final double arrivalElevationM = arrival.getElevation().getMeters();

        appendProfileSegment(json, "climb",
                segment.getStartLatitude(), segment.getStartLongitude(), departureElevationM,
                segment.getStartLatitude(), segment.getStartLongitude(), cruiseAltitudeM,
                true);
        appendProfileSegment(json, "cruise",
                segment.getStartLatitude(), segment.getStartLongitude(), cruiseAltitudeM,
                segment.getEndLatitude(), segment.getEndLongitude(), cruiseAltitudeM,
                true);
        appendProfileSegment(json, "descend",
                segment.getEndLatitude(), segment.getEndLongitude(), cruiseAltitudeM,
                segment.getEndLatitude(), segment.getEndLongitude(), arrivalElevationM,
                false);
    }

    private void appendProfileSegment(final StringBuilder json,
                                      final String mode,
                                      final double startLat,
                                      final double startLon,
                                      final double startAltM,
                                      final double endLat,
                                      final double endLon,
                                      final double endAltM,
                                      final boolean trailingComma) {
        json.append("        {\n");
        json.append("          \"Mode\": \"").append(mode).append("\",\n");
        json.append("          \"Start\": ").append(position(startLat, startLon, startAltM)).append(",\n");
        json.append("          \"End\": ").append(position(endLat, endLon, endAltM)).append("\n");
        json.append("        }");
        if (trailingComma) {
            json.append(",");
        }
        json.append("\n");
    }

    private void appendDslSegment(final StringBuilder json,
                                  final CreateFlightPlanSegmentDTO segment,
                                  final Airport departure,
                                  final Airport arrival,
                                  final int segmentIndex,
                                  final int segmentCount,
                                  final boolean trailingComma) {
        final double cruiseAltitudeM = averageAltitudeMeters(segment.getAltitudeSlots());
        final double startAltitudeM = resolveStartAltitude(
                departure, arrival, segmentIndex, segmentCount, cruiseAltitudeM);
        final double endAltitudeM = resolveEndAltitude(
                departure, arrival, segmentIndex, segmentCount, cruiseAltitudeM);
        final String mode = inferMode(startAltitudeM, endAltitudeM);

        json.append("        {\n");
        json.append("          \"Mode\": \"").append(mode).append("\",\n");
        json.append("          \"Start\": ").append(position(
                segment.getStartLatitude(),
                segment.getStartLongitude(),
                startAltitudeM)).append(",\n");
        json.append("          \"End\": ").append(position(
                segment.getEndLatitude(),
                segment.getEndLongitude(),
                endAltitudeM)).append("\n");
        json.append("        }");
        if (trailingComma) {
            json.append(",");
        }
        json.append("\n");
    }

    private double resolveStartAltitude(final Airport departure,
                                        final Airport arrival,
                                        final int segmentIndex,
                                        final int segmentCount,
                                        final double cruiseAltitudeM) {
        if (segmentIndex == 0) {
            return departure.getElevation().getMeters();
        }
        if (segmentIndex == segmentCount - 1 && segmentCount > 1) {
            return cruiseAltitudeM;
        }
        return cruiseAltitudeM;
    }

    private double resolveEndAltitude(final Airport departure,
                                      final Airport arrival,
                                      final int segmentIndex,
                                      final int segmentCount,
                                      final double cruiseAltitudeM) {
        if (segmentIndex == segmentCount - 1) {
            return arrival.getElevation().getMeters();
        }
        return cruiseAltitudeM;
    }

    private String inferMode(final double startAltitudeM, final double endAltitudeM) {
        if (endAltitudeM > startAltitudeM + 10.0) {
            return "climb";
        }
        if (endAltitudeM < startAltitudeM - 10.0) {
            return "descend";
        }
        return "cruise";
    }

    private double averageAltitudeMeters(final List<CreateFlightPlanAltitudeSlotDTO> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("Segment must define at least one altitude slot.");
        }
        final CreateFlightPlanAltitudeSlotDTO slot = slots.get(0);
        final double averageFeet = (slot.getMinimumAltitude() + slot.getMaximumAltitude()) / 2.0;
        return averageFeet * FEET_TO_METERS;
    }

    private Airport airport(final IataCodeAirport code) {
        return airportRepository.ofIdentity(code)
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + code.code()));
    }

    private String formatTime(final Integer hour, final Integer minute) {
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }

    private String formatSpeed(final Double speed) {
        return String.format(Locale.ROOT, "%.1f", speed == null ? 460.0 : speed);
    }

    private String position(final double latitude, final double longitude, final double altitudeM) {
        return String.format(
                Locale.ROOT,
                "{ \"Latitude\": %.6f, \"Longitude\": %.6f, \"Altitude\": { \"Quantity\": %.1f, \"Unit\": \"m\" } }",
                latitude,
                longitude,
                altitudeM
        );
    }
}
