package pt.isep.sem4pi.infrastructure.remoteaccess;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;

public final class RemoteAccessLogger {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 9000;

    private final String host;
    private final int port;
    private final Clock clock;

    public RemoteAccessLogger(final String host, final int port) {
        this(host, port, Clock.systemDefaultZone());
    }

    RemoteAccessLogger(final String host, final int port, final Clock clock) {
        this.host = host == null || host.isBlank() ? DEFAULT_HOST : host.trim();
        this.port = port;
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
    }

    public static RemoteAccessLogger defaultLogger() {
        return new RemoteAccessLogger(resolveHost(), resolvePort());
    }

    public static String resolveHost() {
        final String property = System.getProperty("remote.access.logging.host");
        if (property != null && !property.isBlank()) {
            return property.trim();
        }
        final String env = System.getenv("REMOTE_ACCESS_LOGGING_HOST");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return DEFAULT_HOST;
    }

    public static int resolvePort() {
        final String property = System.getProperty("remote.access.logging.port");
        if (property != null && !property.isBlank()) {
            return Integer.parseInt(property.trim());
        }
        final String env = System.getenv("REMOTE_ACCESS_LOGGING_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env.trim());
        }
        return DEFAULT_PORT;
    }

    public void log(
            final String username,
            final String clientIp,
            final int clientPort,
            final String service,
            final RemoteAccessEventType eventType) {
        try {
            final RemoteAccessLogEvent event = new RemoteAccessLogEvent(
                    LocalDateTime.now(clock), username, clientIp, clientPort, service, eventType);
            final byte[] payload = event.toPayload().getBytes(StandardCharsets.UTF_8);
            final InetAddress address = InetAddress.getByName(host);
            try (DatagramSocket socket = new DatagramSocket()) {
                final DatagramPacket packet = new DatagramPacket(payload, payload.length, new InetSocketAddress(address, port));
                socket.send(packet);
            }
        } catch (final Exception ignored) {
            // best effort
        }
    }
}