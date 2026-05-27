package pt.isep.sem4pi.us120;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    public Void visitFlightPlan(US120Parser.FlightPlanContext ctx) {
        String sourceName = ctx.getStart().getTokenSource().getSourceName();
        if (sourceName != null && !sourceName.isBlank() && !"<unknown>".equals(sourceName)
                && !sourceName.endsWith(REQUIRED_EXTENSION)) {
            addError(ctx.getStart().getLine(),
                    "Invalid file extension for '" + sourceName + "' (expected " + REQUIRED_EXTENSION + ")");
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitDate(US120Parser.DateContext ctx) {
        String date = ctx.getText();
        String[] parts = date.split("/");
        if (parts.length != 3) {
            addError(ctx.getStart().getLine(), "Invalid date: " + date);
            return null;
        }

        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            LocalDate.of(year, month, day);
        } catch (NumberFormatException | DateTimeException e) {
            addError(ctx.getStart().getLine(), "Invalid date: " + date);
        }
        return null;
    }

    @Override
    public Void visitTime(US120Parser.TimeContext ctx) {
        String time = ctx.getText();
        String[] parts = time.split(":");
        if (parts.length < 2 || parts.length > 3) {
            addError(ctx.getStart().getLine(), "Invalid time: " + time);
            return null;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                addError(ctx.getStart().getLine(), "Invalid time: " + time);
            }
        } catch (NumberFormatException e) {
            addError(ctx.getStart().getLine(), "Invalid time: " + time);
        }
        return null;
    }

    @Override
    public Void visitWindAngle(US120Parser.WindAngleContext ctx) {
        String angleText = ctx.getText();
        try {
            double angle = Double.parseDouble(angleText);
            if (angle < 0 || angle >= 360) {
                addError(ctx.getStart().getLine(), "Invalid angle: " + angleText + " (expected 0 <= angle < 360)");
            }
        } catch (NumberFormatException e) {
            addError(ctx.getStart().getLine(), "Invalid angle: " + angleText);
        }
        return null;
    }

    @Override
    public Void visitAltSlot(US120Parser.AltSlotContext ctx) {
        double min = Double.parseDouble(ctx.number(0).getText());
        double max = Double.parseDouble(ctx.number(1).getText());
        if (min > max) {
            addError(ctx.getStart().getLine(), "Invalid altitude slot: min altitude is greater than max altitude");
        }
        return null;
    }

    @Override
    public Void visitWidth(US120Parser.WidthContext ctx) {
        if (Double.parseDouble(ctx.getText()) <= 0) {
            addError(ctx.getStart().getLine(), "Invalid width: must be > 0");
        }
        return null;
    }

    @Override
    public Void visitSpeed(US120Parser.SpeedContext ctx) {
        if (Double.parseDouble(ctx.getText()) <= 0) {
            addError(ctx.getStart().getLine(), "Invalid speed: must be > 0");
        }
        return null;
    }

    private void addError(int line, String message) {
        errors.add("Line " + line + ": " + message);
    }
}

