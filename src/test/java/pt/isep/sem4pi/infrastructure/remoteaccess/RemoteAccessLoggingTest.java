package pt.isep.sem4pi.infrastructure.remoteaccess;

import org.testng.annotations.Test;
import pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessLoggingServer;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class RemoteAccessLoggingTest {

    @Test
    public void payloadFormattingIncludesAllFields() {
        final LocalDateTime timestamp = LocalDateTime.of(2026, 6, 10, 15, 30, 22);
        final RemoteAccessLogEvent event = new RemoteAccessLogEvent(
                timestamp,
                "mariana",
                "192.168.1.50",
                51522,
                "US44",
                RemoteAccessEventType.LOGIN_SUCCESS);

        assertEquals(event.toPayload(), "2026-06-10T15:30:22;mariana;192.168.1.50;51522;US44;LOGIN_SUCCESS");
    }

    @Test
    public void loggerDoesNotThrowWhenUdpSendFails() {
        final RemoteAccessLogger logger = new RemoteAccessLogger("bad host", 9000, Clock.fixed(Instant.parse("2026-06-10T15:30:22Z"), ZoneOffset.UTC));
        logger.log("mariana", "192.168.1.50", 51522, "US44", RemoteAccessEventType.LOGIN_FAILED);
        assertTrue(true);
    }

    @Test
    public void serverReceivesAndStoresLogLine() throws Exception {
        final Path logFile = Files.createTempFile("remote-access-", ".log");
        final int port = freePort();
        final RemoteAccessLoggingServer server = new RemoteAccessLoggingServer(port, logFile);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (final Exception ignored) {
                // test server stopped
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(200);

        final RemoteAccessLogger logger = new RemoteAccessLogger("127.0.0.1", port, Clock.fixed(Instant.parse("2026-06-10T15:30:22Z"), ZoneOffset.UTC));
        logger.log("mariana", "192.168.1.50", 51522, "US44", RemoteAccessEventType.LOGIN_SUCCESS);

        final String line = awaitLine(logFile);
        assertTrue(line.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2};mariana;192\\.168\\.1\\.50;51522;US44;LOGIN_SUCCESS"),
                "unexpected log line: " + line);

        server.stop();
        serverThread.join(2000);
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static String awaitLine(final Path file) throws Exception {
        for (int i = 0; i < 50; i++) {
            final var lines = Files.readAllLines(file);
            if (!lines.isEmpty()) {
                return lines.get(0).trim();
            }
            Thread.sleep(100);
        }
        throw new AssertionError("No log line received in " + file);
    }
}