package pt.isep.sem4pi.us081;

import org.antlr.v4.runtime.tree.ParseTree;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanAltitudeSlotDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanSegmentDTO;
import pt.isep.sem4pi.us120.US120BaseVisitor;
import pt.isep.sem4pi.us120.US120Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class US081FlightPlanMapper extends US120BaseVisitor<Void> {

    private String flightId;
    private String flightType;
    private String aircraftRegistration;
    private String pilotId;
    private String routeId;
    private final List<CreateFlightPlanLegDTO> legs = new ArrayList<>();

    private US081FlightPlanMapper() {
    }

    public static CreateFlightPlanDTO map(final ParseTree tree) {
        final US081FlightPlanMapper mapper = new US081FlightPlanMapper();
        mapper.visit(tree);
        return new CreateFlightPlanDTO(
                null,
                mapper.routeId,
                mapper.flightId,
                mapper.flightType,
                mapper.aircraftRegistration,
                mapper.pilotId,
                List.copyOf(mapper.legs)
        );
    }

    @Override
    public Void visitFlight(final US120Parser.FlightContext ctx) {
        flightId = ctx.ID().getText();
        flightType = ctx.flightType().getText().toUpperCase(Locale.ROOT);
        visit(ctx.aircraft());
        visit(ctx.pilot());
        visit(ctx.route());
        for (final US120Parser.LegContext legCtx : ctx.leg()) {
            visit(legCtx);
        }
        return null;
    }

    @Override
    public Void visitAircraft(final US120Parser.AircraftContext ctx) {
        aircraftRegistration = ctx.registration().getText();
        return null;
    }

    @Override
    public Void visitPilot(final US120Parser.PilotContext ctx) {
        pilotId = ctx.pilotId().getText();
        return null;
    }

    @Override
    public Void visitRoute(final US120Parser.RouteContext ctx) {
        routeId = ctx.ID().getText();
        return null;
    }

    @Override
    public Void visitLeg(final US120Parser.LegContext ctx) {
        final US120Parser.DepartureContext departure = ctx.departure();
        final US120Parser.ArrivalContext arrival = ctx.arrival();
        final US120Parser.FuelContext fuel = ctx.fuel();

        final int[] departureDate = parseDate(departure.date().getText());
        final int[] departureTime = parseTime(departure.time().getText());
        final int[] arrivalTime = parseTime(arrival.time().getText());

        final List<CreateFlightPlanSegmentDTO> segments = new ArrayList<>();
        for (final US120Parser.SegmentContext segmentCtx : ctx.segment()) {
            segments.add(mapSegment(segmentCtx));
        }

        legs.add(new CreateFlightPlanLegDTO(
                departure.airport().getText(),
                departureDate[0],
                departureDate[1],
                departureDate[2],
                departureTime[0],
                departureTime[1],
                departureTime[2],
                arrival.airport().getText(),
                arrivalTime[0],
                arrivalTime[1],
                arrivalTime[2],
                Double.parseDouble(fuel.number().getText()),
                mapFuelUnit(fuel.unit().getText()),
                List.copyOf(segments)
        ));
        return null;
    }

    private static CreateFlightPlanSegmentDTO mapSegment(final US120Parser.SegmentContext ctx) {
        final double[] start = parseCoord(ctx.beginNode().coord());
        final double[] end = parseCoord(ctx.endNode().coord());
        final List<CreateFlightPlanAltitudeSlotDTO> altitudeSlots = new ArrayList<>();
        for (final US120Parser.AltSlotContext slotCtx : ctx.altSlot()) {
            altitudeSlots.add(new CreateFlightPlanAltitudeSlotDTO(
                    (int) Double.parseDouble(slotCtx.number(0).getText()),
                    (int) Double.parseDouble(slotCtx.number(1).getText())
            ));
        }
        return new CreateFlightPlanSegmentDTO(
                start[0],
                start[1],
                end[0],
                end[1],
                List.copyOf(altitudeSlots),
                Double.parseDouble(ctx.windAngle().getText()),
                Double.parseDouble(ctx.speed().getText())
        );
    }

    private static double[] parseCoord(final US120Parser.CoordContext coord) {
        final String[] parts = coord.getText().split("\\|");
        return new double[]{
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1])
        };
    }

    private static int[] parseDate(final String date) {
        final String[] parts = date.split("/");
        return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        };
    }

    private static int[] parseTime(final String time) {
        final String[] parts = time.split(":");
        final int hour = Integer.parseInt(parts[0]);
        final int minute = Integer.parseInt(parts[1]);
        final int second = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
        return new int[]{hour, minute, second};
    }

    private static String mapFuelUnit(final String unit) {
        return switch (unit) {
            case "kg" -> "KG";
            case "l" -> "L";
            case "m" -> "M";
            case "m/s" -> "MS";
            default -> unit.toUpperCase(Locale.ROOT);
        };
    }
}
