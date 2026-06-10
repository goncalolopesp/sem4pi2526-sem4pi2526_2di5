package pt.isep.sem4pi.domain.model.weatherdata;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class WeatherTimeFormatter {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("HH:mm:ss");

    private WeatherTimeFormatter() {
        // utility
    }

    public static LocalTime normalize(final LocalTime time) {
        if (time == null) {
            return null;
        }
        return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond());
    }

    public static String format(final LocalTime time) {
        if (time == null) {
            return "";
        }
        return normalize(time).format(DISPLAY);
    }
}
