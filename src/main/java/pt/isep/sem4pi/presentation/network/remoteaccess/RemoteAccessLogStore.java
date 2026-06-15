package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe in-memory view model used by US091.
 *
 * <p>It keeps a bounded newest-first event history and derives active sessions
 * from US090 log events. Synchronized methods are required because UDP
 * ingestion and HTTP request threads access the same state concurrently.</p>
 */
public final class RemoteAccessLogStore {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int maximumEvents;
    private final Deque<LogEntry> events = new ArrayDeque<>();
    private final Map<SessionKey, ActiveUser> activeUsers = new LinkedHashMap<>();

    public RemoteAccessLogStore(final int maximumEvents) {
        if (maximumEvents <= 0) {
            throw new IllegalArgumentException("maximumEvents must be positive");
        }
        this.maximumEvents = maximumEvents;
    }

    /**
     * Replays a persisted log so the HTTP view survives server restarts.
     *
     * @param logFile US090 semicolon-separated log
     * @throws IOException when an existing file cannot be read
     */
    public synchronized void load(final Path logFile) throws IOException {
        if (!Files.exists(logFile)) {
            return;
        }
        for (final String line : Files.readAllLines(logFile, StandardCharsets.UTF_8)) {
            record(line);
        }
    }

    /**
     * Parses and applies one log event.
     *
     * <p>{@code LOGIN_SUCCESS} creates/replaces a session, while
     * {@code LOGOUT} and {@code DISCONNECT} remove the matching session.
     * Business-operation and failed-login events remain visible but do not
     * alter active-user state.</p>
     *
     * @param line semicolon-separated US090 event
     * @return {@code false} when the line is malformed and was ignored
     */
    public synchronized boolean record(final String line) {
        final LogEntry entry = LogEntry.parse(line);
        if (entry == null) {
            return false;
        }

        events.addFirst(entry);
        while (events.size() > maximumEvents) {
            events.removeLast();
        }

        final SessionKey key = new SessionKey(
                entry.username(), entry.clientIp(), entry.clientPort(), entry.service());
        if ("LOGIN_SUCCESS".equals(entry.eventType())) {
            activeUsers.put(key, new ActiveUser(
                    entry.username(), entry.clientIp(), entry.clientPort(),
                    entry.service(), entry.timestamp()));
        } else if ("LOGOUT".equals(entry.eventType()) || "DISCONNECT".equals(entry.eventType())) {
            activeUsers.remove(key);
        }
        return true;
    }

    /**
     * @return defensive newest-first copy of the visible event history
     */
    public synchronized List<LogEntry> recentEvents() {
        return new ArrayList<>(events);
    }

    /**
     * @return defensive copy of sessions currently considered active
     */
    public synchronized List<ActiveUser> activeUsers() {
        return new ArrayList<>(activeUsers.values());
    }

    /**
     * Parsed representation of one persisted remote-access event.
     */
    public record LogEntry(
            LocalDateTime timestamp,
            String username,
            String clientIp,
            int clientPort,
            String service,
            String eventType) {

        private static LogEntry parse(final String line) {
            if (line == null || line.isBlank()) {
                return null;
            }
            final String[] fields = line.trim().split(";", -1);
            if (fields.length != 6) {
                return null;
            }
            try {
                return new LogEntry(
                        LocalDateTime.parse(fields[0], TIMESTAMP_FORMAT),
                        fields[1],
                        fields[2],
                        Integer.parseInt(fields[3]),
                        fields[4],
                        fields[5]);
            } catch (final DateTimeParseException | NumberFormatException ex) {
                return null;
            }
        }

        public String formattedTimestamp() {
            return timestamp.format(TIMESTAMP_FORMAT);
        }
    }

    /**
     * Snapshot of a session opened by a successful login.
     */
    public record ActiveUser(
            String username,
            String clientIp,
            int clientPort,
            String service,
            LocalDateTime connectedSince) {

        public String formattedConnectedSince() {
            return connectedSince.format(TIMESTAMP_FORMAT);
        }
    }

    private record SessionKey(String username, String clientIp, int clientPort, String service) {
    }
}
