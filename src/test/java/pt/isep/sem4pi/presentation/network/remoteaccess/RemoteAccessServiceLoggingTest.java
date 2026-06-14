package pt.isep.sem4pi.presentation.network.remoteaccess;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.bootstrap.MasterUsersBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;
import pt.isep.sem4pi.infrastructure.remoteaccess.RemoteAccessEventType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class RemoteAccessServiceLoggingTest {

    private static RemoteAccessLoggingServer loggingServer;
    private static Thread loggingThread;
    private static RemoteAccessTcpServer server;
    private static Path auditFile;
    private static int port;

    @BeforeClass
    public static void startServers() throws Exception {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());
        PersistenceContext.resetForTesting();
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder());
        new MasterUsersBootstrapper().execute();

        auditFile = Files.createTempFile("remote-service-audit-", ".log");
        final int loggingPort = freeUdpPort();
        System.setProperty("remote.access.logging.port", Integer.toString(loggingPort));
        loggingServer = new RemoteAccessLoggingServer(loggingPort, auditFile);
        loggingThread = new Thread(() -> {
            try {
                loggingServer.start();
            } catch (final Exception ignored) {
                // server stopped
            }
        });
        loggingThread.setDaemon(true);
        loggingThread.start();
        Thread.sleep(200);

        port = freeTcpPort();
        server = new RemoteAccessTcpServer(port);
        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (final Exception ignored) {
                // server stopped
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        waitUntilListening(port);
    }

    @AfterClass
    public static void stopServers() {
        if (server != null) {
            server.stop();
        }
        if (loggingServer != null) {
            loggingServer.stop();
        }
        if (loggingThread != null) {
            try {
                loggingThread.join(2000);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.clearProperty("remote.access.logging.port");
    }

    @Test
    public void pilotAccessLifecycleIsLoggedAsUs86() throws Exception {
        verifyAccessLifecycle("pilot", "PILOT", "US86");
    }

    @Test
    public void atccAccessLifecycleIsLoggedAsUs78() throws Exception {
        verifyAccessLifecycle("atccollaborator", "ATCC", "US78");
    }

    private static void verifyAccessLifecycle(
            final String username,
            final String service,
            final String userStory) throws Exception {
        try (Connection connection = new Connection(port)) {
            connection.read();
            connection.send("LOGIN " + username + " wrong-password " + service);
            assertTrue(connection.read().startsWith(RemoteAccessProtocol.ERR));
            awaitEvent(username, userStory, RemoteAccessEventType.LOGIN_FAILED);

            connection.send("LOGIN " + username + " Password1 " + service);
            assertTrue(connection.read().startsWith(RemoteAccessProtocol.OK));
            connection.read();
            awaitEvent(username, userStory, RemoteAccessEventType.LOGIN_SUCCESS);

            connection.send("SELECT " + service);
            assertTrue(connection.read().startsWith(RemoteAccessProtocol.OK));
            connection.send(RemoteAccessProtocol.CMD_LOGOUT);
            assertTrue(connection.read().startsWith(RemoteAccessProtocol.OK));
            awaitEvent(username, userStory, RemoteAccessEventType.LOGOUT);
        }
        awaitEvent(username, userStory, RemoteAccessEventType.DISCONNECT);
    }

    private static void awaitEvent(
            final String username,
            final String userStory,
            final RemoteAccessEventType eventType) throws Exception {
        for (int i = 0; i < 100; i++) {
            final List<String> lines = Files.readAllLines(auditFile);
            final boolean found = lines.stream().anyMatch(line ->
                    line.contains(";" + username + ";")
                            && line.endsWith(";" + userStory + ";" + eventType.name()));
            if (found) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Missing " + eventType + " audit event for " + userStory);
    }

    private static int freeTcpPort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static int freeUdpPort() throws Exception {
        try (DatagramSocket socket = new DatagramSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void waitUntilListening(final int serverPort) throws Exception {
        for (int i = 0; i < 100; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", serverPort), 200);
                return;
            } catch (final Exception e) {
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException("Server did not start on port " + serverPort);
    }

    private static final class Connection implements AutoCloseable {
        private final Socket socket;
        private final BufferedReader in;
        private final BufferedWriter out;

        private Connection(final int serverPort) throws Exception {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", serverPort), 2000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        private void send(final String message) throws Exception {
            out.write(message);
            out.write('\n');
            out.flush();
        }

        private String read() throws Exception {
            return in.readLine();
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }
}
