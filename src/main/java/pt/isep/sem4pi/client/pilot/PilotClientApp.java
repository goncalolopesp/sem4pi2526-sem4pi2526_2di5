package pt.isep.sem4pi.client.pilot;

import pt.isep.sem4pi.presentation.network.remoteaccess.PilotRemoteHandler;
import pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessProtocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public final class PilotClientApp {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = RemoteAccessProtocol.DEFAULT_PORT;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final String SERVICE_PILOT = "PILOT";

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Scanner console = new Scanner(System.in);

    private String username;
    private Set<String> roles = new HashSet<>();
    private boolean connected = true;

    private PilotClientApp(final BufferedReader in, final BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    public static void main(final String[] args) {
        final String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("Connecting to Pilot remote access at " + host + ":" + port + " ...");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setKeepAlive(true);
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter out = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

                final String greeting = in.readLine();
                if (greeting != null) {
                    System.out.println(greeting);
                }
                new PilotClientApp(in, out).run();
            }
        } catch (final IOException e) {
            System.err.println("Could not communicate with the server: " + e.getMessage());
        }
    }

    private void run() throws IOException {
        while (true) {
            if (!loginLoop()) {
                sendLine("QUIT");
                readLine();
                System.out.println("Goodbye.");
                return;
            }
            if (sessionMenu()) {
                return;
            }
        }
    }

    private boolean loginLoop() throws IOException {
        while (true) {
            System.out.println();
            System.out.println("=== Login (leave username empty to quit) ===");
            System.out.print("Username: ");
            final String user = console.nextLine().trim();
            if (user.isEmpty()) {
                return false;
            }
            System.out.print("Password: ");
            final String pass = console.nextLine();

            sendLine("LOGIN " + user + " " + pass);
            final String response = readLine();
            if (response == null) {
                System.out.println("Connection closed by server.");
                return false;
            }
            if (response.startsWith(RemoteAccessProtocol.OK)) {
                parseLogin(response, user);
                readLine();
                if (!selectPilotService()) {
                    System.out.println("Pilot service is not available for your role.");
                    sendLine("LOGOUT");
                    readLine();
                    continue;
                }
                System.out.println("Welcome " + username + " (roles: " + String.join(", ", roles) + ")");
                return true;
            }
            System.out.println(response);
        }
    }

    private boolean selectPilotService() throws IOException {
        sendLine("SELECT " + SERVICE_PILOT);
        final String response = readLine();
        if (response == null || !response.startsWith(RemoteAccessProtocol.OK)) {
            if (response != null) {
                System.out.println(response);
            }
            return false;
        }
        System.out.println(response);
        return true;
    }

    private void parseLogin(final String response, final String fallbackUser) {
        final String[] parts = response.split("\\s+");
        this.username = parts.length >= 2 ? parts[1] : fallbackUser;
        this.roles = new HashSet<>();
        if (parts.length >= 3 && !"-".equals(parts[2])) {
            this.roles.addAll(Arrays.asList(parts[2].split(",")));
        }
    }

    private boolean sessionMenu() throws IOException {
        while (true) {
            printMenu();
            final String option = console.nextLine().trim();
            try {
                switch (option) {
                    case "1" -> listRoutes();
                    case "2" -> listFlights();
                    case "3" -> listMyPlans();
                    case "4" -> createFlightPlan();
                    case "5" -> importFlightPlan();
                    case "6" -> listWeather();
                    case "7" -> associateWeather();
                    case "8" -> listDraftPlans();
                    case "9" -> testFlightPlan();
                    case "L" -> {
                        logoutQuietly();
                        return false;
                    }
                    case "0" -> {
                        if (connected) {
                            sendLine("QUIT");
                            System.out.println(readLine());
                        }
                        return true;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (final IOException e) {
                handleCommunicationError(e);
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Pilot App (US86) [" + username + "] ===");
        System.out.println("1. List routes");
        System.out.println("2. List flights");
        System.out.println("3. List my flight plans (US082 context)");
        System.out.println("4. Create flight plan (US080)");
        System.out.println("5. Import flight plan from file (US081)");
        System.out.println("6. List weather data (US082)");
        System.out.println("7. Associate weather with plan (US082)");
        System.out.println("8. List draft plans (US085)");
        System.out.println("9. Test/validate flight plan (US085)");
        System.out.println("L. Logout");
        System.out.println("0. Quit");
        System.out.print("Option: ");
    }

    private void listRoutes() throws IOException {
        sendLine(PilotRemoteHandler.CMD_LIST_ROUTES);
        printTable(readLine());
    }

    private void listFlights() throws IOException {
        sendLine(PilotRemoteHandler.CMD_LIST_FLIGHTS);
        printTable(readLine());
    }

    private void listMyPlans() throws IOException {
        sendLine(PilotRemoteHandler.CMD_LIST_MY_PLANS);
        printTable(readLine());
    }

    private void createFlightPlan() throws IOException {
        System.out.print("Route ID: ");
        final String routeId = console.nextLine().trim();
        System.out.print("Flight ID: ");
        final String flightId = console.nextLine().trim();
        System.out.print("Flight type (REGULAR/CHARTER): ");
        final String flightType = console.nextLine().trim();
        System.out.print("Pilot ID: ");
        final String pilotId = console.nextLine().trim();
        System.out.print("Dep day: ");
        final String depDay = console.nextLine().trim();
        System.out.print("Dep month: ");
        final String depMonth = console.nextLine().trim();
        System.out.print("Dep year: ");
        final String depYear = console.nextLine().trim();
        System.out.print("Dep hour: ");
        final String depH = console.nextLine().trim();
        System.out.print("Dep minute: ");
        final String depM = console.nextLine().trim();
        System.out.print("Dep second: ");
        final String depS = console.nextLine().trim();
        System.out.print("Arr hour: ");
        final String arrH = console.nextLine().trim();
        System.out.print("Arr minute: ");
        final String arrM = console.nextLine().trim();
        System.out.print("Arr second: ");
        final String arrS = console.nextLine().trim();
        System.out.print("Fuel quantity: ");
        final String fuel = console.nextLine().trim();
        System.out.print("Fuel unit (KG/L): ");
        final String unit = console.nextLine().trim();

        sendLine(String.join(" ", PilotRemoteHandler.CMD_CREATE_FLIGHT_PLAN,
                routeId, flightId, flightType, pilotId,
                depDay, depMonth, depYear, depH, depM, depS,
                arrH, arrM, arrS, fuel, unit));
        System.out.println(readLine());
    }

    private void importFlightPlan() throws IOException {
        System.out.print("DSL file path: ");
        final String path = console.nextLine().trim();
        final List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path));
        } catch (final IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
            return;
        }
        sendLine(PilotRemoteHandler.CMD_IMPORT_PLAN_BEGIN + " " + lines.size());
        final String ack = readLine();
        if (ack == null || !ack.startsWith(RemoteAccessProtocol.OK)) {
            System.out.println(ack);
            return;
        }
        for (final String line : lines) {
            sendLine(line);
        }
        String response = readLine();
        System.out.println(response);
        if (response != null && response.startsWith(RemoteAccessProtocol.ERR)) {
            String errorLine;
            while ((errorLine = readLine()) != null
                    && !errorLine.startsWith(RemoteAccessProtocol.OK)) {
                System.out.println(errorLine);
            }
        }
    }

    private void listWeather() throws IOException {
        sendLine(PilotRemoteHandler.CMD_LIST_WEATHER);
        printTable(readLine());
    }

    private void associateWeather() throws IOException {
        System.out.print("Flight plan ID: ");
        final String planId = console.nextLine().trim();
        System.out.print("Weather data IDs (comma-separated): ");
        final String ids = console.nextLine().trim();
        sendLine(PilotRemoteHandler.CMD_ASSOCIATE_WEATHER + " " + planId + " " + ids);
        System.out.println(readLine());
    }

    private void listDraftPlans() throws IOException {
        sendLine(PilotRemoteHandler.CMD_LIST_DRAFT_PLANS);
        printTable(readLine());
    }

    private void testFlightPlan() throws IOException {
        System.out.print("Flight plan ID: ");
        final String planId = console.nextLine().trim();
        sendLine(PilotRemoteHandler.CMD_TEST_FLIGHT_PLAN + " " + planId);
        System.out.println(readLine());
    }

    private void printTable(final String header) throws IOException {
        if (header == null) {
            System.out.println("Connection closed by server.");
            return;
        }
        System.out.println(header);
        if (!header.startsWith(RemoteAccessProtocol.OK)) {
            return;
        }
        final String[] parts = header.split("\\s+");
        if (parts.length < 2) {
            return;
        }
        final int count = Integer.parseInt(parts[1]);
        for (int i = 0; i < count; i++) {
            System.out.println(readLine());
        }
    }

    private void handleCommunicationError(final IOException e) {
        connected = false;
        System.out.println("Communication error: " + e.getMessage());
    }

    private void logoutQuietly() throws IOException {
        sendLine("BACK");
        readLine();
        readLine();
        sendLine("LOGOUT");
        final String response = readLine();
        if (response != null) {
            System.out.println(response);
        }
        username = null;
        roles = new HashSet<>();
    }

    private void sendLine(final String message) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server.");
        }
        out.write(message);
        out.write('\n');
        out.flush();
    }

    private String readLine() throws IOException {
        if (!connected) {
            return null;
        }
        return in.readLine();
    }
}
