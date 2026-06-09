package pt.isep.sem4pi.presentation.network.weather;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class WeatherServerLog {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private WeatherServerLog() {
        // utility
    }

    public static void log(final String message) {
        System.out.println("[" + LocalDateTime.now().format(TIMESTAMP) + "] [WeatherServer] " + message);
    }

    public static String summarizeIncoming(final String line) {
        if (line == null || line.isEmpty()) {
            return "(empty line)";
        }
        final String command = firstToken(line).toUpperCase();
        if (WeatherProtocol.CMD_LOGIN.equals(command)) {
            return "LOGIN <credentials omitted>";
        }
        if (WeatherProtocol.CMD_IMPORT.equals(command)) {
            final String[] parts = line.split("\\s+", 4);
            if (parts.length >= 4) {
                return "IMPORT " + parts[1] + " " + parts[2] + " <" + parts[3].length() + " base64 chars>";
            }
            return "IMPORT (incomplete)";
        }
        if (WeatherProtocol.CMD_IMPORT_BEGIN.equals(command)) {
            return line;
        }
        if (line.length() > 200) {
            return line.substring(0, 200) + "...";
        }
        return line;
    }

    private static String firstToken(final String line) {
        final int idx = line.indexOf(' ');
        return idx < 0 ? line : line.substring(0, idx);
    }
}
