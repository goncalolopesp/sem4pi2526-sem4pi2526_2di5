package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class RemoteAccessLoggingServer {

    private static final int DEFAULT_PORT = 9000;
    private static final String DEFAULT_LOG_FILE = "remote-access.log";

    private final int port;
    private final Path logFile;

    private volatile boolean running;
    private DatagramSocket socket;

    public RemoteAccessLoggingServer(final int port, final Path logFile) {
        this.port = port;
        this.logFile = logFile;
    }

    public static void main(final String[] args) throws Exception {
        final int port = resolvePort(args);
        final Path logFile = resolveLogFile(args);
        new RemoteAccessLoggingServer(port, logFile).start();
    }

    public void start() throws IOException {
        final Path parent = logFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        socket = new DatagramSocket(port);
        socket.setSoTimeout(500);
        running = true;
        while (running) {
            try {
                final byte[] buffer = new byte[8192];
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                final String line = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                Files.writeString(logFile, line + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (final java.net.SocketTimeoutException ignored) {
                // keep waiting until stop() is called
            }
        }
    }

    public void stop() {
        running = false;
        if (socket != null) {
            socket.close();
        }
    }

    private static int resolvePort(final String[] args) {
        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return Integer.parseInt(args[0].trim());
        }
        final String property = System.getProperty("remote.access.logging.server.port");
        if (property != null && !property.isBlank()) {
            return Integer.parseInt(property.trim());
        }
        final String env = System.getenv("REMOTE_ACCESS_LOGGING_SERVER_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env.trim());
        }
        return DEFAULT_PORT;
    }

    private static Path resolveLogFile(final String[] args) {
        if (args != null && args.length > 1 && !args[1].isBlank()) {
            return Path.of(args[1].trim());
        }
        final String property = System.getProperty("remote.access.logging.server.logFile");
        if (property != null && !property.isBlank()) {
            return Path.of(property.trim());
        }
        final String env = System.getenv("REMOTE_ACCESS_LOGGING_SERVER_LOGFILE");
        if (env != null && !env.isBlank()) {
            return Path.of(env.trim());
        }
        return Path.of(DEFAULT_LOG_FILE);
    }
}