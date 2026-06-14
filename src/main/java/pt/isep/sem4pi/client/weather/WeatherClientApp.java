package pt.isep.sem4pi.client.weather;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import pt.isep.sem4pi.presentation.network.weather.WeatherDataFileResolver;
import pt.isep.sem4pi.presentation.network.weather.WeatherProtocol;

import pt.isep.sem4pi.application.controller.aircraft.AddAircraftController;
import pt.isep.sem4pi.application.controller.aircraft.RetireAircraftController;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCompanyFleetController;
import pt.isep.sem4pi.application.controller.route.CreateFlightRouteController;
import pt.isep.sem4pi.application.controller.route.DeactivateFlightRouteController;

import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;

import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public final class WeatherClientApp {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8044;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_WEATHER_PERSON = "WEATHER_PERSON";
    private static final String ROLE_PILOT = "PILOT";
    private static final String ROLE_FLIGHT_CONTROL_OPERATOR = "FLIGHT_CONTROL_OPERATOR";
    private static final String ROLE_AIR_TRANSPORT_COMPANY_COLLABORATOR = "AIR_TRANSPORT_COMPANY_COLLABORATOR";

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Scanner console = new Scanner(System.in);

    private String username;
    private Set<String> roles = new HashSet<>();
    private boolean connected = true;

    private WeatherClientApp(final BufferedReader in, final BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    public static void main(final String[] args) {
        final String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("Connecting to remote access server at " + host + ":" + port + " ...");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setKeepAlive(true);
            socket.setSendBufferSize(256 * 1024);
            socket.setReceiveBufferSize(256 * 1024);
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter out = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

                final String greeting = in.readLine();
                if (greeting != null) {
                    System.out.println(greeting);
                }
                new WeatherClientApp(in, out).run();
            }
        } catch (final IOException e) {
            System.err.println("Could not communicate with the server: " + e.getMessage());
        }
    }

    private void run() throws IOException {
        boolean exitProgram = false;
        while (!exitProgram) {
            final boolean loggedIn = loginLoop();
            if (!loggedIn) {
                sendLine("QUIT");
                readLine();
                System.out.println("Goodbye.");
                return;
            }
            exitProgram = sessionMenu();
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
            if (response.startsWith("OK")) {
                parseLogin(response, user);
                System.out.println("Welcome " + username + " (roles: " + String.join(", ", roles) + ")");
                return true;
            }
            System.out.println(response);
        }
    }

    private void parseLogin(final String response, final String fallbackUser) {
        final String[] parts = response.split("\\s+");
        this.username = parts.length >= 2 ? parts[1] : fallbackUser;
        this.roles = new HashSet<>();
        if (parts.length >= 3 && !"-".equals(parts[2])) {
            this.roles.addAll(Arrays.asList(parts[2].split(",")));
        }
    }

    private boolean isWeatherManager() {
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_WEATHER_PERSON);
    }

    private boolean isWeatherConsultant() {
        return isWeatherManager()
                || roles.contains(ROLE_PILOT)
                || roles.contains(ROLE_FLIGHT_CONTROL_OPERATOR);
    }

    private boolean isAirTransportCollaborator() {
        return roles.contains(ROLE_AIR_TRANSPORT_COMPANY_COLLABORATOR)
                || roles.contains(ROLE_ADMIN);
    }

    private boolean sessionMenu() throws IOException {
        while (true) {
            printMenu();
            final String option = console.nextLine().trim();

            if (isWeatherManager() && ("1".equals(option) || "2".equals(option))) {
                try {
                    if ("1".equals(option)) {
                        registerWeather();
                    } else {
                        importWeather();
                    }
                } catch (final IOException e) {
                    handleCommunicationError(e);
                }
                continue;
            }
            if (isWeatherConsultant() && "3".equals(option)) {
                try {
                    consultWeather();
                } catch (final IOException e) {
                    handleCommunicationError(e);
                }
                continue;
            }
            if (isAirTransportCollaborator()) {

                switch (option) {

                    case "4" -> {
                        addAircraft();
                        continue;
                    }

                    case "5" -> {
                        retireAircraft();
                        continue;
                    }

                    case "6" -> {
                        listFleet();
                        continue;
                    }

                    case "7" -> {
                        createFlightRoute();
                        continue;
                    }

                    case "8" -> {
                        deactivateFlightRoute();
                        continue;
                    }

                    case "11" -> {
                        System.out.println("US075 not implemented.");
                        continue;
                    }

                    case "12" -> {
                        System.out.println("US076 not implemented.");
                        continue;
                    }

                    case "13" -> {
                        System.out.println("US077 not implemented.");
                        continue;
                    }
                }
            }

            switch (option) {
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
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Remote Access [" + username + "] ===");
        final boolean manager = isWeatherManager();
        final boolean consultant = isWeatherConsultant();
        final boolean collaborator = isAirTransportCollaborator();

        boolean hasOperations = false;

        if (manager || consultant) {
            hasOperations = true;
            System.out.println("--- Weather Data ---");
            if (manager) {
                System.out.println("1. Register weather data (US041)");
                System.out.println("2. Import bulk weather data (US042)");
            }
            if (consultant) {
                System.out.println("3. Consult weather data (US043)");
            }
        }
        if (collaborator) {

            hasOperations = true;

            System.out.println("--- Air Transport Company ---");
            System.out.println("4. Add aircraft (US070)");
            System.out.println("5. Retire aircraft (US071)");
            System.out.println("6. List fleet (US072)");
            System.out.println("7. Create flight route (US073)");
            System.out.println("8. Deactivate flight route (US074)");

            System.out.println("11. Add pilot (US075) (Not implemented)");
            System.out.println("12. List pilot roster (US076) (Not implemented)");
            System.out.println("13. Remove pilot (US077) (Not implemented)");
        }

        if (!hasOperations) {
            System.out.println("(No remote operations available for your role.)");
        }
        System.out.println("9. Logout");
        System.out.println("0. Quit");
        System.out.print("Option: ");
    }

    private void registerWeather() throws IOException {
        System.out.print("Air Control Area ID: ");
        final String areaId = console.nextLine().trim();
        System.out.print("Weather Data Date (yyyy-MM-dd): ");
        final String dateInput = console.nextLine().trim();
        System.out.print("Start Time (HH:mm): ");
        final String startTimeInput = console.nextLine().trim();
        System.out.print("End Time (HH:mm, blank to use start time): ");
        final String endTimeInput = console.nextLine().trim();
        final LocalDate date;
        final LocalTime startTime;
        final LocalTime endTime;
        try {
            date = LocalDate.parse(dateInput);
            startTime = LocalTime.parse(startTimeInput, TIME_FORMAT);
            endTime = endTimeInput.isEmpty()
                    ? startTime
                    : LocalTime.parse(endTimeInput, TIME_FORMAT);
        } catch (final DateTimeParseException e) {
            System.out.println("Invalid date or time format. Use yyyy-MM-dd and HH:mm.");
            return;
        }
        final String temp = ask("Temperature (Celsius)");
        final String windSpeed = ask("Wind speed (knots)");
        final String windDir = ask("Wind direction (0-359)");
        final String humidity = ask("Humidity (%)");
        final String pressure = ask("Pressure (hPa)");
        final String altitude = ask("Altitude (meters)");

        sendLine(String.join(" ", "REGISTER", areaId, dateInput,
                startTime.format(TIME_FORMAT), endTime.format(TIME_FORMAT),
                temp, windSpeed, windDir, humidity, pressure, altitude));
        System.out.println(readLine());
    }

    private void importWeather() throws IOException {
        System.out.print("Enter file path or name (extension optional): ");
        final String userInput = console.nextLine().trim();
        final Optional<WeatherDataFileResolver.ResolvedWeatherFile> resolved;
        try {
            resolved = WeatherDataFileResolver.resolve(userInput);
        } catch (final IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
            return;
        }
        if (resolved.isEmpty()) {
            System.out.println("File not found: " + userInput);
            return;
        }

        final Path path = resolved.get().path();
        final String fileType = resolved.get().fileType();
        System.out.println("Using file: " + path + " (" + fileType + ")");

        final byte[] content;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
            return;
        }

        final String base64 = Base64.getEncoder().encodeToString(content);
        final List<String> chunks = splitBase64(base64, WeatherProtocol.IMPORT_BASE64_CHUNK_SIZE);
        final String typeToken = fileType.isEmpty() ? WeatherProtocol.AUTODETECT : fileType;
        final String fileName = path.getFileName().toString();

        sendLine("IMPORT_BEGIN " + fileName + " " + typeToken + " " + chunks.size());
        final String ack = readLine();
        if (ack == null || !ack.startsWith(WeatherProtocol.OK)) {
            System.out.println(ack != null ? ack : "Connection closed by server.");
            return;
        }

        System.out.println("Uploading file (" + chunks.size() + " chunks)...");
        for (final String chunk : chunks) {
            sendLine(chunk);
        }

        String header = readImportResponse();
        if (header == null || !header.startsWith("OK")) {
            System.out.println(header);
            return;
        }
        final String[] parts = header.split("\\s+");
        if (parts.length >= 5) {
            System.out.println("Import completed.");
            System.out.println("  Total records: " + parts[1]);
            System.out.println("  Imported:      " + parts[2]);
            System.out.println("  Rejected:      " + parts[3]);
            final int errCount = Integer.parseInt(parts[4]);
            if (errCount > 0) {
                System.out.println("  Errors:");
                for (int i = 0; i < errCount; i++) {
                    System.out.println("   - " + readLine());
                }
            }
        } else {
            System.out.println(header);
        }
    }

    private void consultWeather() throws IOException {
        System.out.print("Air Control Area ID: ");
        final String areaId = console.nextLine().trim();
        System.out.print("Date (yyyy-MM-dd): ");
        final String date = console.nextLine().trim();
        sendLine("CONSULT " + areaId + " " + date);

        final String header = readLine();
        if (header == null) {
            System.out.println("Connection closed by server.");
            return;
        }
        if (!header.startsWith(WeatherProtocol.OK)) {
            System.out.println(header);
            return;
        }
        final String[] parts = header.split("\\s+");
        if (parts.length >= 2 && "0".equals(parts[1])) {
            System.out.println("No weather data found for area " + areaId + " on " + date + ".");
            return;
        }
        final int count = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
        if (count == 0) {
            System.out.println("No weather data found for area " + areaId + " on " + date + ".");
            return;
        }
        System.out.printf("%-12s %-10s %-10s %-9s %-12s %-9s %-12s %-9s %-10s%n",
                "Date", "Start", "End", "Temp(C)", "Wind(kts)", "Dir", "Press(hPa)", "Hum(%)", "Alt(m)");
        for (int i = 0; i < count; i++) {
            final String row = readLine();
            if (row == null) {
                break;
            }
            final String[] f = row.split(";");
            if (f.length >= 9) {
                System.out.printf("%-12s %-10s %-10s %-9s %-12s %-9s %-12s %-9s %-10s%n",
                        f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8]);
            } else {
                System.out.println(row);
            }
        }
        System.out.println();
    }

    private String ask(final String label) {
        System.out.print(label + ": ");
        return console.nextLine().trim();
    }

    private void handleCommunicationError(final IOException e) {
        connected = false;
        System.out.println("Communication error: " + e.getMessage());
        System.out.println("Connection lost. Use option 9 (Logout) and login again.");
    }

    private void logoutQuietly() throws IOException {
        if (connected) {
            sendLine("LOGOUT");
            final String response = readLine();
            if (response != null) {
                System.out.println(response);
            }
        } else {
            System.out.println("Session ended (connection was lost).");
        }
        connected = false;
    }

    private void sendLine(final String message) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server.");
        }
        try {
            out.write(message);
            out.write('\n');
            out.flush();
        } catch (final IOException e) {
            connected = false;
            throw e;
        }
    }

    private String readLine() throws IOException {
        if (!connected) {
            return null;
        }
        try {
            return in.readLine();
        } catch (final IOException e) {
            connected = false;
            throw e;
        }
    }

    private String readImportResponse() throws IOException {
        String line = readLine();
        while (line != null && line.contains(WeatherProtocol.IMPORT_IN_PROGRESS)) {
            System.out.println("Server is processing import, please wait...");
            line = readLine();
        }
        return line;
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

    private void addAircraft() {
        try {

            System.out.print("Company IATA: ");
            String company = console.nextLine();

            System.out.print("Registration: ");
            String reg = console.nextLine();

            System.out.print("Manufacturer: ");
            String manufacturer = console.nextLine();

            System.out.print("Model: ");
            String model = console.nextLine();

            System.out.print("Country: ");
            String country = console.nextLine();

            System.out.print("Economy seats: ");
            int e = Integer.parseInt(console.nextLine());

            System.out.print("Business seats: ");
            int b = Integer.parseInt(console.nextLine());

            System.out.print("First seats: ");
            int f = Integer.parseInt(console.nextLine());

            AddAircraftController controller =
                    new AddAircraftController(
                            PersistenceContext.repositories().airTransportCompanies(),
                            PersistenceContext.repositories().aircraftModels()
                    );

            controller.addAircraft(company, reg, manufacturer, model, country, e, b, f);

            System.out.println("Aircraft added successfully.");

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void retireAircraft() {
        try {
            System.out.print("Aircraft registration: ");
            String reg = console.nextLine();

            RetireAircraftController controller =
                    new RetireAircraftController();

            controller.retireAircraft(
                    new AircraftRegistration(reg)
            );

            System.out.println("Aircraft retired successfully.");

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void listFleet() {

        try {
            ListCompanyFleetController controller =
                    new ListCompanyFleetController();

            Iterable<Aircraft> fleet =
                    controller.listMyCompanyFleet(
                            ListCompanyFleetController.FleetSortOption.REGISTRATION
                    );

            System.out.println("=== Fleet ===");

            for (Aircraft a : fleet) {
                System.out.println(a);
            }

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void createFlightRoute() {

        try {
            System.out.print("Route ID: ");
            String routeId = console.nextLine();

            System.out.print("Company IATA: ");
            String company = console.nextLine();

            System.out.print("Origin airport: ");
            String origin = console.nextLine();

            System.out.print("Destination airport: ");
            String destination = console.nextLine();

            FlightRouteDTO dto = new FlightRouteDTO();

            dto.setRouteID(routeId);
            dto.setCompanyCode(company);
            dto.setOriginAirport(origin);
            dto.setDestinationAirport(destination);

            CreateFlightRouteController controller =
                    new CreateFlightRouteController();

            controller.createFlightRoute(dto);

            System.out.println("Flight route created successfully.");

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void deactivateFlightRoute() {

        try {
            System.out.print("Route ID: ");
            String routeId = console.nextLine();

            System.out.print("Deactivation date (yyyy-mm-dd): ");
            String date = console.nextLine();

            DeactivateFlightRouteController controller =
                    new DeactivateFlightRouteController();

            controller.deactivateRoute(
                    routeId,
                    LocalDate.parse(date)
            );

            System.out.println("Route deactivated successfully.");

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
