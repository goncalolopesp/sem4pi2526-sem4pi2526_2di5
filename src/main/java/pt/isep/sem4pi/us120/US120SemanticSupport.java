package pt.isep.sem4pi.us120;

import pt.isep.sem4pi.us120.US120Parser.AltSlotContext;
import pt.isep.sem4pi.us120.US120Parser.CoordContext;
import pt.isep.sem4pi.us120.US120Parser.DateContext;
import pt.isep.sem4pi.us120.US120Parser.TimeContext;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

final class US120SemanticSupport {

    private static final double COORD_EPSILON = 1e-6;

    private US120SemanticSupport() {
    }

    static double[] parseCoord(final CoordContext ctx) {
        final String[] parts = ctx.getText().split("\\|");
        return new double[]{
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1])
        };
    }

    static LocalDate parseDate(final DateContext ctx) {
        final String[] parts = ctx.getText().split("/");
        return LocalDate.of(
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[0])
        );
    }

    static LocalTime parseTime(final TimeContext ctx) {
        final String[] parts = ctx.getText().split(":");
        final int hour = Integer.parseInt(parts[0]);
        final int minute = Integer.parseInt(parts[1]);
        final int second = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
        return LocalTime.of(hour, minute, second);
    }

    static double[] parseAltitudeSlotBounds(final AltSlotContext ctx) {
        return new double[]{
                Double.parseDouble(ctx.number(0).getText()),
                Double.parseDouble(ctx.number(1).getText())
        };
    }

    static boolean sameCoordinates(final double[] a, final double[] b) {
        return Math.abs(a[0] - b[0]) < COORD_EPSILON && Math.abs(a[1] - b[1]) < COORD_EPSILON;
    }

    static boolean differentCoordinates(final double[] a, final double[] b) {
        return !sameCoordinates(a, b);
    }

    static List<double[]> routeCoordinates(final US120Parser.RouteContext ctx) {
        final List<double[]> coordinates = new ArrayList<>();
        for (final US120Parser.RoutesContext routePoint : ctx.routes()) {
            coordinates.add(parseCoord(routePoint.coord()));
        }
        return coordinates;
    }

    static String formatError(final int line, final String message) {
        return "Line " + line + ": " + message;
    }

    static boolean isValidCalendarDate(final DateContext ctx) {
        try {
            parseDate(ctx);
            return true;
        } catch (NumberFormatException | DateTimeException | ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }
}
