package pt.isep.sem4pi.us120;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import pt.isep.sem4pi.us120.US120BaseVisitor;
import pt.isep.sem4pi.us120.US120Parser;

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
                    "Extensão de ficheiro inválida para '" + sourceName + "' (esperado " + REQUIRED_EXTENSION + ")");
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitDate(US120Parser.DateContext ctx) {
        String date = ctx.getText();
        String[] parts = date.split("/");
        if (parts.length != 3) {
            addError(ctx.getStart().getLine(), "Data inválida: " + date);
            return null;
        }

        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            LocalDate.of(year, month, day);
        } catch (NumberFormatException | DateTimeException e) {
            addError(ctx.getStart().getLine(), "Data inválida/inexistente: " + date);
        }
        return null;
    }

    @Override
    public Void visitTime(US120Parser.TimeContext ctx) {
        String time = ctx.getText();
        String[] parts = time.split(":");
        if (parts.length < 2 || parts.length > 3) {
            addError(ctx.getStart().getLine(), "Formato de hora inválido: " + time);
            return null;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                addError(ctx.getStart().getLine(), "Valores de tempo fora dos limites: " + time);
            }
        } catch (NumberFormatException e) {
            addError(ctx.getStart().getLine(), "Tempo inválido: " + time);
        }
        return null;
    }

    @Override
    public Void visitWindAngle(US120Parser.WindAngleContext ctx) {
        String angleText = ctx.getText();
        try {
            double angle = Double.parseDouble(angleText);
            if (angle < 0 || angle >= 360) {
                addError(ctx.getStart().getLine(), "Ângulo de vento inválido: " + angleText + " (esperado 0 <= ângulo < 360)");
            }
        } catch (NumberFormatException e) {
            addError(ctx.getStart().getLine(), "Ângulo de vento não numérico: " + angleText);
        }
        return null;
    }

    @Override
    public Void visitAltSlot(US120Parser.AltSlotContext ctx) {
        double min = Double.parseDouble(ctx.number(0).getText());
        double max = Double.parseDouble(ctx.number(1).getText());
        if (min > max) {
            addError(ctx.getStart().getLine(), "Slot de altitude inválido: altitude mínima é maior que a máxima");
        }
        return null;
    }

    @Override
    public Void visitWidth(US120Parser.WidthContext ctx) {
        if (Double.parseDouble(ctx.getText()) <= 0) {
            addError(ctx.getStart().getLine(), "Largura inválida: tem de ser > 0");
        }
        return null;
    }

    @Override
    public Void visitSpeed(US120Parser.SpeedContext ctx) {
        if (Double.parseDouble(ctx.getText()) <= 0) {
            addError(ctx.getStart().getLine(), "Velocidade inválida: tem de ser > 0");
        }
        return null;
    }

    @Override
    public Void visitCoord(US120Parser.CoordContext ctx) {
        String text = ctx.getText();
        String[] parts = text.split("\\|");

        if (parts.length == 2) {
            try {
                double lat = Double.parseDouble(parts[0]);
                double lon = Double.parseDouble(parts[1]);

                if (lat < -90.0 || lat > 90.0) {
                    addError(ctx.getStart().getLine(), "Latitude inválida: " + lat + " (deve estar entre -90 e 90)");
                }

                if (lon < -180.0 || lon > 180.0) {
                    addError(ctx.getStart().getLine(), "Longitude inválida: " + lon + " (deve estar entre -180 e 180)");
                }
            } catch (NumberFormatException e) {
                addError(ctx.getStart().getLine(), "Coordenadas com formato numérico inválido: " + text);
            }
        } else {
            addError(ctx.getStart().getLine(), "Formato de coordenada incorreto (esperado lat|lon): " + text);
        }
        return null;
    }

    private void addError(int line, String message) {
        errors.add("Linha " + line + ": " + message);
    }
}