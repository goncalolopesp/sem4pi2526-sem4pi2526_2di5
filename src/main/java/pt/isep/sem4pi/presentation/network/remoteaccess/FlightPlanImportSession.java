package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.util.ArrayList;
import java.util.List;

final class FlightPlanImportSession {

    static final String SEND_LINES = "SEND_LINES";

    private final int lineCount;
    private final List<String> lines = new ArrayList<>();

    FlightPlanImportSession(final int lineCount) {
        this.lineCount = lineCount;
    }

    int lineCount() {
        return lineCount;
    }

    void addLine(final String line) {
        lines.add(line);
    }

    boolean isComplete() {
        return lines.size() >= lineCount;
    }

    String content() {
        return String.join(System.lineSeparator(), lines);
    }
}
