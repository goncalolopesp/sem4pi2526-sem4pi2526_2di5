package pt.isep.sem4pi.presentation.network.weather;

import pt.isep.sem4pi.infrastructure.remoteaccess.RemoteAccessLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WeatherTcpServer {

    private final int port;
    private final ExecutorService workers;
    private final RemoteAccessLogger remoteAccessLogger;
    private final String serviceIdentifier;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public WeatherTcpServer(final int port) {
        this(port, RemoteAccessLogger.defaultLogger(), "US44");
    }

    public WeatherTcpServer(final int port, final RemoteAccessLogger remoteAccessLogger, final String serviceIdentifier) {
        this.port = port;
        this.workers = Executors.newCachedThreadPool();
        this.remoteAccessLogger = remoteAccessLogger == null ? RemoteAccessLogger.defaultLogger() : remoteAccessLogger;
        this.serviceIdentifier = serviceIdentifier == null || serviceIdentifier.isBlank()
                ? "US44"
                : serviceIdentifier.trim();
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.running = true;
        WeatherServerLog.log("Listening on TCP port " + port + " ...");

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        while (running) {
            try {
                final Socket client = serverSocket.accept();
                client.setKeepAlive(true);
                client.setReceiveBufferSize(256 * 1024);
                client.setSendBufferSize(256 * 1024);
                workers.submit(new WeatherClientHandler(client, remoteAccessLogger, serviceIdentifier));
            } catch (final IOException e) {
                if (running) {
                    WeatherServerLog.log("Accept failed: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        if (!running) {
            return;
        }
        this.running = false;
        WeatherServerLog.log("Shutting down ...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (final IOException ignored) {
            // best effort
        }
        workers.shutdownNow();
    }
}
