package pt.isep.sem4pi.presentation.network.remoteaccess;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.bootstrap.MasterUsersBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.AirTransportCompanyBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.CompanyFleetBootstrapper;
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

import static org.junit.Assert.assertTrue;

public class AirTransportCompanyRemoteAccessIT {

    private static RemoteAccessTcpServer server;
    private static int port;

    @BeforeClass
    public static void startServer() throws Exception {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());
        PersistenceContext.resetForTesting();

        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder()
        );
        new MasterUsersBootstrapper().execute();
        new AirTransportCompanyBootstrapper(PersistenceContext.repositories().airTransportCompanies()).execute();
        new CompanyFleetBootstrapper().execute();
        ensureAtccCollaboratorProfile();

        port = freePort();
        server = new RemoteAccessTcpServer(port);
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
    public void atccOperationsRequireAuthentication() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send(AirTransportCompanyRemoteHandler.CMD_LIST_FLEET);
            assertTrue(c.read().startsWith(RemoteAccessProtocol.ERR));
        }
    }

    @Test
    public void atccOperationsRequireServiceSelection() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN atccollaborator Password1");
            assertTrue(c.read().startsWith(RemoteAccessProtocol.OK));
            assertTrue(c.read().startsWith("OK MENU"));

            c.send(AirTransportCompanyRemoteHandler.CMD_LIST_FLEET);
            assertTrue(c.read().startsWith(RemoteAccessProtocol.ERR));
        }
    }

    @Test
    public void weatherUserCannotSelectAtccService() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN weather Password1");
            assertTrue(c.read().startsWith(RemoteAccessProtocol.OK));
            assertTrue(c.read().startsWith("OK MENU"));

            c.send("SELECT ATCC");
            assertTrue(c.read().startsWith(RemoteAccessProtocol.ERR));
        }
    }

    @Test
    public void listFleetWorksForCollaborator() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.loginAsAtccCollaborator();
            c.send(AirTransportCompanyRemoteHandler.CMD_LIST_FLEET);
            final String header = c.read();
            assertTrue("expected fleet header, got: " + header, header.startsWith(RemoteAccessProtocol.OK));
        }
    }

    @Test
    public void loginLogsUs78WhenOnlyAtccServiceAvailable() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN atccollaborator Password1");
            final String login = c.read();
            assertTrue(login.startsWith(RemoteAccessProtocol.OK));
            assertTrue(login.contains("AIR_TRANSPORT_COMPANY_COLLABORATOR"));
            assertTrue(c.read().startsWith("OK MENU ATCC"));
        }
    }

    private static void ensureAtccCollaboratorProfile() {
        final var users = PersistenceContext.repositories().users();
        final var companies = PersistenceContext.repositories().airTransportCompanies();
        final var collabRepo = PersistenceContext.repositories().airTransportCompanyCollaborators();

        final Username atccUsername = Username.valueOf("atccollaborator");
        final var user = users.ofIdentity(atccUsername)
                .orElseThrow(() -> new IllegalStateException("atccollaborator user missing from bootstrap"));

        final AirTransportCompany company = companies.findByIataCode(new IataCodeCompany("TP"))
                .orElseGet(() -> companies.save(new AirTransportCompany(
                        new CompanyName("TAP Air Portugal"),
                        new IataCodeCompany("TP"),
                        new IcaoCodeCompany("TAP"))));

        if (collabRepo.findBySystemUser(user).isEmpty()) {
            collabRepo.save(new AirTransportCompanyCollaborator(user, company));
        }
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

        String greeting() throws Exception {
            return read();
        }

        void loginAsAtccCollaborator() throws Exception {
            send("LOGIN atccollaborator Password1");
            assertTrue(read().startsWith(RemoteAccessProtocol.OK));
            assertTrue(read().startsWith("OK MENU"));
            send("SELECT ATCC");
            assertTrue(read().startsWith("OK Selected"));
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
