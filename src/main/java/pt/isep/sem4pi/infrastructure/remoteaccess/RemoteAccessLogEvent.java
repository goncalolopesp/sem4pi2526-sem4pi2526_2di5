package pt.isep.sem4pi.infrastructure.remoteaccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record RemoteAccessLogEvent(
        LocalDateTime timestamp,
        String username,
        String clientIp,
        int clientPort,
        String service,
        RemoteAccessEventType eventType) {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public RemoteAccessLogEvent {
        timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
        username = normalize(username);
        clientIp = normalize(clientIp);
        service = normalize(service);
    }

    public String toPayload() {
        return String.join(";",
                timestamp.format(TIMESTAMP_FORMAT),
                username,
                clientIp,
                String.valueOf(clientPort),
                service,
                eventType == null ? "-" : eventType.name());
    }

    private static String normalize(final String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}