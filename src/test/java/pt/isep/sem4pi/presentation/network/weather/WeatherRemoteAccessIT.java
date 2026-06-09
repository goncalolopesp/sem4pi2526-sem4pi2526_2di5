package pt.isep.sem4pi.presentation.network.weather;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.bootstrap.MasterUsersBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class WeatherRemoteAccessIT {

    private static WeatherTcpServer server;
    private static int port;

    @BeforeClass
    public static void startServer() throws Exception {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder()
        );
        new MasterUsersBootstrapper().execute();

        port = freePort();
        server = new WeatherTcpServer(port);
        final Thread t = new Thread(() -> {
            try {
                server.start();
            } catch (final Exception ignored) {
                // server stopped
            }
        });
        t.setDaemon(true);
        t.start();
        waitUntilListening(port);
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void helpWorksWithoutLogin() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("HELP");
            assertTrue(c.read().startsWith("OK"));
        }
    }

    @Test
    public void consultWithNoResultsReturnsNoData() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN weather Password1");
            assertTrue(c.read().startsWith("OK"));

            c.send("CONSULT CW001 2099-01-01");
            final String response = c.read();
            assertTrue(response.startsWith("OK 0"), "expected empty consult response, got: " + response);
            assertTrue(response.contains(WeatherProtocol.NO_DATA));
        }
    }

    @Test
    public void operationsRequireAuthentication() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("CONSULT CW001 2024-01-01");
            assertTrue(c.read().startsWith("ERR"));
        }
    }

    @Test
    public void wrongCredentialsAreRejected() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN weather wrongpass");
            assertTrue(c.read().startsWith("ERR"));
        }
    }

    @Test
    public void otherRoleLogsInButCannotRunWeatherOperations() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN backoffice Password1");
            final String login = c.read();
            assertTrue(login.startsWith("OK"), "other roles must be allowed to log in");

            c.send("CONSULT CW001 " + LocalDate.now());
            assertTrue(c.read().startsWith("ERR"),
                    "weather operations must be denied for non-weather roles");
        }
    }

    @Test
    public void chunkedImportWorks() throws Exception {
        final Path file = Path.of(
                "src", "main", "java", "pt", "isep", "sem4pi", "data", "weatherData", "CW_WeatherData_v0.xlsx");
        final byte[] content = Files.readAllBytes(file);
        final String base64 = Base64.getEncoder().encodeToString(content);
        final List<String> chunks = splitBase64(base64, WeatherProtocol.IMPORT_BASE64_CHUNK_SIZE);

        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN weather Password1");
            assertTrue(c.read().startsWith("OK"));

            c.send("IMPORT_BEGIN CW_WeatherData_v0.xlsx xlsx " + chunks.size());
            assertTrue(c.read().contains(WeatherProtocol.SEND_CHUNKS));

            for (final String chunk : chunks) {
                c.send(chunk);
            }

            final String header = c.read();
            assertTrue(header.startsWith("OK"), "expected import summary, got: " + header);
        }
    }

    @Test
    public void registerAndConsultRoundTrip() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN weather Password1");
            assertTrue(c.read().startsWith("OK"));

            c.send("REGISTER CW001 " + LocalDate.now() + " 15 10 90 50 1013 1000");
            assertTrue(c.read().startsWith("OK"));

            c.send("CONSULT CW001 " + LocalDate.now());
            final String header = c.read();
            assertTrue(header.startsWith("OK"));
            final int count = Integer.parseInt(header.split("\\s+")[1]);
            assertTrue(count >= 1, "expected at least one record");
            for (int i = 0; i < count; i++) {
                assertNotNull(c.read());
            }
        }
    }

    private static List<String> splitBase64(final String base64, final int chunkSize) {
        final List<String> chunks = new ArrayList<>();
        if (base64.isEmpty()) {
            chunks.add("");
            return chunks;
        }
        for (int i = 0; i < base64.length(); i += chunkSize) {
            chunks.add(base64.substring(i, Math.min(i + chunkSize, base64.length())));
        }
        return chunks;
    }

    private static int freePort() throws Exception {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    private static void waitUntilListening(final int port) throws Exception {
        for (int i = 0; i < 100; i++) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", port), 200);
                return;
            } catch (final Exception e) {
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException("Server did not start on port " + port);
    }

    private static final class Conn implements AutoCloseable {
        private final Socket socket;
        private final BufferedReader in;
        private final BufferedWriter out;

        Conn(final int port) throws Exception {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", port), 2000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        void greeting() throws Exception {
            read();
        }

        void send(final String line) throws Exception {
            out.write(line);
            out.write('\n');
            out.flush();
        }

        String read() throws Exception {
            return in.readLine();
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }
}
