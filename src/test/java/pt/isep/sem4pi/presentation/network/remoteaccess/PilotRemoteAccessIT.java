package pt.isep.sem4pi.presentation.network.remoteaccess;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.model.flight.FlightType;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.bootstrap.MasterUsersBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.PilotBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.time.LocalDate;
import java.util.List;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class PilotRemoteAccessIT {

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
        seedPilotTestData();
        new PilotBootstrapper().execute();

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
    public void pilotOperationsRequireAuthentication() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send(PilotRemoteHandler.CMD_LIST_ROUTES);
            assertTrue(c.read().startsWith(RemoteAccessProtocol.ERR));
        }
    }

    @Test
    public void pilotOperationsRequireServiceSelection() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN pilot Password1");
            assertTrue(c.read().startsWith(RemoteAccessProtocol.OK));
            assertTrue(c.read().startsWith("OK MENU"));

            c.send(PilotRemoteHandler.CMD_LIST_ROUTES);
            assertTrue(c.read().startsWith(RemoteAccessProtocol.ERR));
        }
    }

    @Test
    public void weatherUserCannotSelectPilotService() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN weather Password1");
            assertTrue(c.read().startsWith(RemoteAccessProtocol.OK));
            assertTrue(c.read().startsWith("OK MENU"));

            c.send("SELECT PILOT");
            assertTrue(c.read().startsWith(RemoteAccessProtocol.ERR));
        }
    }

    @Test
    public void listRoutesWorksForPilot() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.loginAsPilot();
            c.send(PilotRemoteHandler.CMD_LIST_ROUTES);
            final String header = c.read();
            assertTrue("expected routes header, got: " + header, header.startsWith(RemoteAccessProtocol.OK));
        }
    }

    @Test
    public void listMyPlansWorksForPilot() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.loginAsPilot();
            c.send(PilotRemoteHandler.CMD_LIST_MY_PLANS);
            final String header = c.read();
            assertTrue(header.startsWith(RemoteAccessProtocol.OK));
        }
    }

    @Test
    public void loginMenuIncludesPilotService() throws Exception {
        try (Conn c = new Conn(port)) {
            c.greeting();
            c.send("LOGIN pilot Password1");
            final String login = c.read();
            assertTrue(login.startsWith(RemoteAccessProtocol.OK));
            assertTrue(login.contains("PILOT"));
            final String menu = c.read();
            assertTrue(menu.contains("PILOT"));
        }
    }

    private static void seedPilotTestData() {
        final var repositories = PersistenceContext.repositories();
        final IataCodeCompany companyCode = new IataCodeCompany("TP");
        repositories.airTransportCompanies().save(new AirTransportCompany(
                new CompanyName("TAP Air Portugal"),
                companyCode,
                new IcaoCodeCompany("TAP")));

        final FlightRouteID routeId = new FlightRouteID("TP1001");
        repositories.flightRoutes().save(new FlightRoute(
                routeId,
                companyCode,
                new IataCodeAirport("OPO"),
                new IataCodeAirport("MAD"),
                List.of(new RouteSegment(
                        new Coordinates(41.24, -8.67),
                        new Coordinates(40.47, -3.56),
                        List.of(new AltitudeRange(30000, 35000, 10.0)),
                        850,
                        new Wind(45, 10.0)))));

        final AircraftRegistration registration = new AircraftRegistration("CS-TST");
        repositories.aircrafts().save(new Aircraft(
                registration,
                new AircraftModelId(new ManufacturerId("AIRBUS"), "A320"),
                new CabinConfiguration(150, 20, 0),
                "Portugal",
                LocalDate.of(2015, 5, 10)));

        repositories.flights().save(new Flight(
                new FlightId("TP100"),
                registration,
                routeId,
                LocalDate.now().plusDays(5)));

        final LocalDate departureDate = LocalDate.now().plusDays(5);
        repositories.flightPlans().save(new FlightPlan(
                new FlightPlanId("FP-DEMO-001"),
                new FlightId("TP100"),
                FlightType.REGULAR,
                repositories.flightRoutes().ofIdentity(routeId).orElseThrow(),
                List.of(new pt.isep.sem4pi.domain.model.flight.FlightLeg(
                        new Departure(
                                new IataCodeAirport("OPO"),
                                new Date(departureDate.getDayOfMonth(), departureDate.getMonthValue(),
                                        departureDate.getYear()),
                                new Time(9, 0, 0)),
                        new Arrival(new IataCodeAirport("MAD"), new Time(11, 0, 0)),
                        List.of(),
                        new Fuel(new FuelCapacity(4200.0), FuelUnit.KG))),
                repositories.aircrafts().ofIdentity(registration).orElseThrow(),
                new Pilot(new PilotId("PILOT01"), companyCode, "pilot")));
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

        void loginAsPilot() throws Exception {
            send("LOGIN pilot Password1");
            assertTrue(read().startsWith(RemoteAccessProtocol.OK));
            assertTrue(read().startsWith("OK MENU"));
            send("SELECT PILOT");
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
