package pt.isep.sem4pi.presentation.network.remoteaccess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Processo que aloja o recetor UDP de logs da US090 e a visualizacao HTTP da US091.
 *
 * <p>No arranque, as linhas de log anteriores sao carregadas para um
 * {@link RemoteAccessLogStore} em memoria. Cada novo datagrama UDP e anexado
 * ao ficheiro de log e depois registado nesse store, tornando eventos
 * persistidos e eventos em direto visiveis nas mesmas paginas HTTP.</p>
 */
public final class RemoteAccessLoggingServer {

    private static final int DEFAULT_PORT = 9000;
    private static final int DEFAULT_HTTP_PORT = 8081;
    private static final int MAXIMUM_VISIBLE_EVENTS = 100;
    private static final String DEFAULT_LOG_FILE = "remote-access.log";

    private final int port;
    private final Path logFile;
    private final int httpPort;

    private volatile boolean running;
    private DatagramSocket socket;
    private RemoteAccessHttpServer httpServer;

    public RemoteAccessLoggingServer(final int port, final Path logFile) {
        this(port, logFile, DEFAULT_HTTP_PORT);
    }

    public RemoteAccessLoggingServer(final int port, final Path logFile, final int httpPort) {
        this.port = port;
        this.logFile = logFile;
        this.httpPort = httpPort;
    }

    public static void main(final String[] args) throws Exception {
        final int port = resolvePort(args);
        final Path logFile = resolveLogFile(args);
        final int httpPort = resolveHttpPort(args);
        new RemoteAccessLoggingServer(port, logFile, httpPort).start();
    }

    /**
     * Inicia o servidor HTTP de estado e depois recebe eventos UDP ate
     * {@link #stop()} ser chamado.
     *
     * @throws IOException quando o ficheiro de log, socket UDP ou servidor HTTP
     *                     nao consegue ser inicializado
     */
    public void start() throws IOException {
        final Path parent = logFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        final RemoteAccessLogStore store = new RemoteAccessLogStore(MAXIMUM_VISIBLE_EVENTS);
        store.load(logFile);
        httpServer = new RemoteAccessHttpServer(httpPort, store);
        httpServer.start();
        System.out.println("Remote Access status pages: http://localhost:" + httpServer.port() + "/");
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
                store.record(line);
            } catch (final java.net.SocketTimeoutException ignored) {
                // keep waiting until stop() is called
            }
        }
    }

    /**
     * Para ambos os endpoints de rede. Fechar o socket tambem liberta uma
     * operacao de receive bloqueada.
     */
    public void stop() {
        running = false;
        if (socket != null) {
            socket.close();
        }
        if (httpServer != null) {
            httpServer.stop();
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

    private static int resolveHttpPort(final String[] args) {
        if (args != null && args.length > 2 && !args[2].isBlank()) {
            return Integer.parseInt(args[2].trim());
        }
        final String property = System.getProperty("remote.access.logging.server.httpPort");
        if (property != null && !property.isBlank()) {
            return Integer.parseInt(property.trim());
        }
        final String env = System.getenv("REMOTE_ACCESS_LOGGING_SERVER_HTTP_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env.trim());
        }
        return DEFAULT_HTTP_PORT;
    }
}
