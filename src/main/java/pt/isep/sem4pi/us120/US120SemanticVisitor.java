package pt.isep.sem4pi.us120;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import pt.isep.sem4pi.us120.US120Parser.FuelContext;
import pt.isep.sem4pi.us120.US120Parser.NumberContext;

public class US120SemanticVisitor extends US120BaseVisitor<Void> {
    private static final String REQUIRED_EXTENSION = ".dsldig5";
    private final List<String> errors = new ArrayList<>();

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public Void visitFlightPlan(final US120Parser.FlightPlanContext ctx) {
        final String sourceName = ctx.getStart().getTokenSource().getSourceName();
        if (sourceName != null && !sourceName.isBlank() && !"<unknown>".equals(sourceName)
                && !sourceName.endsWith(REQUIRED_EXTENSION)) {
            addError(ctx.getStart().getLine(),
                    "Invalid file extension for '" + sourceName + "' (expected " + REQUIRED_EXTENSION + ")");
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitRegistration(final US120Parser.RegistrationContext ctx) {
        final String registration = ctx.getText();
        if (registration.isBlank()) {
            addError(ctx.getStart().getLine(), "Aircraft registration cannot be empty");
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitPilotId(final US120Parser.PilotIdContext ctx) {
        final String pilotId = ctx.getText();
        if (pilotId.isBlank()) {
            addError(ctx.getStart().getLine(), "Pilot identifier cannot be empty");
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitDate(final US120Parser.DateContext ctx) {
        final String date = ctx.getText();
        final String[] parts = date.split("/");
        if (parts.length != 3) {
            addError(ctx.getStart().getLine(), "Invalid date: " + date);
            return null;
        }

        try {
            final int day = Integer.parseInt(parts[0]);
            final int month = Integer.parseInt(parts[1]);
            final int year = Integer.parseInt(parts[2]);
            LocalDate.of(year, month, day);
        } catch (NumberFormatException | DateTimeException e) {
            addError(ctx.getStart().getLine(), "Invalid or non-existent date: " + date);
        }
        return null;
    }

    @Override
    public Void visitTime(final US120Parser.TimeContext ctx) {
        final String time = ctx.getText();
        final String[] parts = time.split(":");
        if (parts.length < 2 || parts.length > 3) {
            addError(ctx.getStart().getLine(), "Invalid time format: " + time);
            return null;
        }

        try {
            final int hour = Integer.parseInt(parts[0]);
            final int minute = Integer.parseInt(parts[1]);
            final int second = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                addError(ctx.getStart().getLine(), "Time values out of bounds: " + time);
            }
        } catch (NumberFormatException e) {
            addError(ctx.getStart().getLine(), "Invalid time: " + time);
        }
        return null;
    }

    @Override
    public Void visitFuel(final FuelContext ctx) {
        final double quantity = parsePositiveNumber(ctx.number(), "fuel quantity");
        if (quantity <= 0) {
            addError(ctx.getStart().getLine(), "Fuel quantity must be strictly positive.");
        }

        final String unit = ctx.unit().getText();
        if (!"kg".equals(unit) && !"l".equals(unit)) {
            addError(ctx.getStart().getLine(), "Invalid fuel unit '" + unit + "' (expected kg or l).");
        }
        return null;
    }

    @Override
    public Void visitWindAngle(final US120Parser.WindAngleContext ctx) {
        final String angleText = ctx.getText();
        try {
            final double angle = Double.parseDouble(angleText);
            if (angle < 0 || angle >= 360) {
                addError(ctx.getStart().getLine(), "Invalid wind angle: " + angleText + " (expected 0 <= angle < 360)");
            }
        } catch (NumberFormatException e) {
            addError(ctx.getStart().getLine(), "Non-numeric wind angle: " + angleText);
        }
        return null;
    }

    @Override
    public Void visitAltSlot(final US120Parser.AltSlotContext ctx) {
        final double min = parsePositiveNumber(ctx.number(0), "minimum altitude");
        final double max = parsePositiveNumber(ctx.number(1), "maximum altitude");
        if (min <= 0 || max <= 0) {
            addError(ctx.getStart().getLine(), "Altitude slot values must be strictly positive.");
        }
        if (min > max) {
            addError(ctx.getStart().getLine(), "Invalid altitude slot: minimum altitude is greater than maximum");
        }
        return null;
    }

    @Override
    public Void visitWidth(final US120Parser.WidthContext ctx) {
        if (parsePositiveNumber(ctx.number(), "width") <= 0) {
            addError(ctx.getStart().getLine(), "Invalid width: must be > 0");
        }
        return null;
    }

    @Override
    public Void visitSpeed(final US120Parser.SpeedContext ctx) {
        if (parsePositiveNumber(ctx.number(), "speed") <= 0) {
            addError(ctx.getStart().getLine(), "Invalid speed: must be > 0");
        }
        return null;
    }

    @Override
    public Void visitCoord(final US120Parser.CoordContext ctx) {
        final String text = ctx.getText();
        final String[] parts = text.split("\\|");

        if (parts.length == 2) {
            try {
                final double lat = Double.parseDouble(parts[0]);
                final double lon = Double.parseDouble(parts[1]);

                if (lat < -90.0 || lat > 90.0) {
                    addError(ctx.getStart().getLine(), "Invalid latitude: " + lat + " (must be between -90 and 90)");
                }

                if (lon < -180.0 || lon > 180.0) {
                    addError(ctx.getStart().getLine(), "Invalid longitude: " + lon + " (must be between -180 and 180)");
                }
            } catch (NumberFormatException e) {
                addError(ctx.getStart().getLine(), "Coordinates with invalid numeric format: " + text);
            }
        } else {
            addError(ctx.getStart().getLine(), "Incorrect coordinate format (expected lat|lon): " + text);
        }
        return null;
    }

    private double parsePositiveNumber(final NumberContext numberContext, final String label) {
        try {
            return Double.parseDouble(numberContext.getText());
        } catch (NumberFormatException ex) {
            addError(numberContext.getStart().getLine(), "Non-numeric " + label + ": " + numberContext.getText());
            return -1;
        }
    }

    private void addError(final int line, final String message) {
        errors.add(US120SemanticSupport.formatError(line, message));
    }
}
