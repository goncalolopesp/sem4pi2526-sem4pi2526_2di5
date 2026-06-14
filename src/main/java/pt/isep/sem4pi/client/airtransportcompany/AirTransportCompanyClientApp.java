package pt.isep.sem4pi.client.airtransportcompany;

import pt.isep.sem4pi.presentation.network.remoteaccess.AirTransportCompanyRemoteHandler;
import pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessProtocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public final class AirTransportCompanyClientApp {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = RemoteAccessProtocol.DEFAULT_PORT;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final String SERVICE_ATCC = "ATCC";

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Scanner console = new Scanner(System.in);

    private String username;
    private Set<String> roles = new HashSet<>();
    private boolean connected = true;
    private boolean serviceSelected = false;

    private AirTransportCompanyClientApp(final BufferedReader in, final BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    public static void main(final String[] args) {
        final String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("Connecting to Air Transport Company remote access at " + host + ":" + port + " ...");
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
                new AirTransportCompanyClientApp(in, out).run();
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

            sendLine("LOGIN " + user + " " + pass + " " + SERVICE_ATCC);
            final String response = readLine();
            if (response == null) {
                System.out.println("Connection closed by server.");
                return false;
            }
            if (response.startsWith(RemoteAccessProtocol.OK)) {
                parseLogin(response, user);
                readLine(); // MENU line
                if (!selectAtccService()) {
                    System.out.println("Air Transport Company service is not available for your role.");
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

    private boolean selectAtccService() throws IOException {
        sendLine("SELECT " + SERVICE_ATCC);
        final String response = readLine();
        if (response == null || !response.startsWith(RemoteAccessProtocol.OK)) {
            serviceSelected = false;
            if (response != null) {
                System.out.println(response);
            }
            return false;
        }
        serviceSelected = true;
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
                    case "1" -> addAircraft();
                    case "2" -> retireAircraft();
                    case "3" -> listFleet();
                    case "4" -> createFlightRoute();
                    case "5" -> deactivateFlightRoute();
                    case "9" -> {
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
        System.out.println("=== Air Transport Company App (US78) [" + username + "] ===");
        System.out.println("1. Add aircraft (US070)");
        System.out.println("2. Retire aircraft (US071)");
        System.out.println("3. List fleet (US072)");
        System.out.println("4. Create flight route (US073)");
        System.out.println("5. Deactivate flight route (US074)");
        System.out.println("9. Logout");
        System.out.println("0. Quit");
        System.out.print("Option: ");
    }

    private void addAircraft() throws IOException {
        System.out.print("Company IATA: ");
        final String company = console.nextLine().trim();
        System.out.print("Registration: ");
        final String reg = console.nextLine().trim();
        System.out.print("Manufacturer: ");
        final String manufacturer = console.nextLine().trim();
        System.out.print("Model: ");
        final String model = console.nextLine().trim();
        System.out.print("Country: ");
        final String country = console.nextLine().trim();
        System.out.print("Economy seats: ");
        final String eco = console.nextLine().trim();
        System.out.print("Business seats: ");
        final String bus = console.nextLine().trim();
        System.out.print("First seats: ");
        final String first = console.nextLine().trim();

        sendLine(String.join(" ", AirTransportCompanyRemoteHandler.CMD_ADD_AIRCRAFT,
                company, reg, manufacturer, model, country, eco, bus, first));
        System.out.println(readLine());
    }

    private void retireAircraft() throws IOException {
        System.out.print("Aircraft registration: ");
        final String reg = console.nextLine().trim();
        sendLine(AirTransportCompanyRemoteHandler.CMD_RETIRE_AIRCRAFT + " " + reg);
        System.out.println(readLine());
    }

    private void listFleet() throws IOException {
        System.out.println("Sort by: REGISTRATION, MAKER, MODEL, CAPACITY, AGE (blank = REGISTRATION)");
        System.out.print("Sort option: ");
        final String sort = console.nextLine().trim();
        if (sort.isEmpty()) {
            sendLine(AirTransportCompanyRemoteHandler.CMD_LIST_FLEET);
        } else {
            sendLine(AirTransportCompanyRemoteHandler.CMD_LIST_FLEET + " " + sort.toUpperCase());
        }

        final String header = readLine();
        if (header == null) {
            System.out.println("Connection closed by server.");
            return;
        }
        if (!header.startsWith(RemoteAccessProtocol.OK)) {
            System.out.println(header);
            return;
        }
        final String[] parts = header.split("\\s+");
        final int count = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
        if (count == 0) {
            System.out.println("Fleet is empty.");
            return;
        }
        System.out.printf("%-12s %-12s %-12s %-10s %-10s %-5s%n",
                "Registration", "Model", "Maker", "Status", "Capacity", "Age");
        for (int i = 0; i < count; i++) {
            final String row = readLine();
            if (row == null) {
                break;
            }
            final String[] fields = row.split(";");
            if (fields.length >= 6) {
                System.out.printf("%-12s %-12s %-12s %-10s %-10s %-5s%n",
                        fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]);
            } else {
                System.out.println(row);
            }
        }
    }

    private void createFlightRoute() throws IOException {
        System.out.print("Route ID: ");
        final String routeId = console.nextLine().trim();
        System.out.print("Company IATA: ");
        final String company = console.nextLine().trim();
        System.out.print("Origin airport (ICAO): ");
        final String origin = console.nextLine().trim();
        System.out.print("Destination airport (ICAO): ");
        final String destination = console.nextLine().trim();

        sendLine(String.join(" ", AirTransportCompanyRemoteHandler.CMD_CREATE_ROUTE,
                routeId, company, origin, destination));
        System.out.println(readLine());
    }

    private void deactivateFlightRoute() throws IOException {
        System.out.print("Route ID: ");
        final String routeId = console.nextLine().trim();
        System.out.print("Deactivation date (yyyy-MM-dd): ");
        final String date = console.nextLine().trim();
        sendLine(AirTransportCompanyRemoteHandler.CMD_DEACTIVATE_ROUTE + " " + routeId + " " + date);
        System.out.println(readLine());
    }

    private void handleCommunicationError(final IOException e) {
        connected = false;
        serviceSelected = false;
        System.out.println("Communication error: " + e.getMessage());
        System.out.println("Connection lost. Use option 9 (Logout) and login again.");
    }

    private void logoutQuietly() throws IOException {
        if (connected && serviceSelected) {
            sendLine(RemoteAccessProtocol.CMD_BACK);
            readLine();
            readLine(); // refreshed menu
        }
        if (connected) {
            sendLine(RemoteAccessProtocol.CMD_LOGOUT);
            final String response = readLine();
            if (response != null) {
                System.out.println(response);
            }
        } else {
            System.out.println("Session ended (connection was lost).");
        }
        serviceSelected = false;
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
