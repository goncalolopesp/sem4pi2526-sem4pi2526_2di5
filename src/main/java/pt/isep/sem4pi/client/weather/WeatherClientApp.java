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

public final class WeatherClientApp {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8044;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String SERVICE_WEATHER = "WEATHER";
    private static final String SERVICE_PILOT = "PILOT";

    private static final int ACTION_LOGOUT = -1;
    private static final int ACTION_SELECT = 0;
    private static final int ACTION_QUIT = 1;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_WEATHER_PERSON = "WEATHER_PERSON";
    private static final String ROLE_PILOT = "PILOT";
    private static final String ROLE_FLIGHT_CONTROL_OPERATOR = "FLIGHT_CONTROL_OPERATOR";

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Scanner console = new Scanner(System.in);

    private String username;
    private Set<String> roles = new HashSet<>();
    private List<String> availableServices = new ArrayList<>();
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
                parseMenu(readLine());
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

    private void parseMenu(final String menuLine) {
        availableServices.clear();
        if (menuLine == null || !menuLine.startsWith("OK MENU ")) {
            return;
        }
        final String services = menuLine.substring("OK MENU ".length()).trim();
        if (!"-".equals(services) && !services.isEmpty()) {
            availableServices.addAll(Arrays.asList(services.split(",")));
        }
    }

    private boolean sessionMenu() throws IOException {
        while (true) {
            final int action = serviceSelectionMenu();
            if (action == ACTION_QUIT) {
                return true;
            }
            if (action == ACTION_LOGOUT) {
                return false;
            }
            final int weatherAction = weatherOperationsMenu();
            if (weatherAction == ACTION_QUIT) {
                return true;
            }
            if (weatherAction == ACTION_LOGOUT) {
                return false;
            }
        }
    }

    private int serviceSelectionMenu() throws IOException {
        while (true) {
            printServiceMenu();
            final String option = console.nextLine().trim();

            if ("1".equals(option) && availableServices.contains(SERVICE_WEATHER)) {
                sendLine("SELECT " + SERVICE_WEATHER);
                final String response = readLine();
                if (response == null) {
                    System.out.println("Connection closed by server.");
                    connected = false;
                    return ACTION_LOGOUT;
                }
                if (response.startsWith("OK")) {
                    System.out.println(response);
                    return ACTION_SELECT;
                }
                System.out.println(response);
                continue;
            }

            if ("2".equals(option) && roles.contains(ROLE_PILOT)) {
                pilotOperationsMenu();
                continue;
            }

            switch (option) {
                case "9" -> {
                    logoutQuietly();
                    return ACTION_LOGOUT;
                }
                case "0" -> {
                    if (connected) {
                        sendLine("QUIT");
                        System.out.println(readLine());
                    }
                    return ACTION_QUIT;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private int weatherOperationsMenu() throws IOException {
        while (true) {
            printWeatherMenu();
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

            switch (option) {
                case "8" -> {
                    sendLine("BACK");
                    final String response = readLine();
                    if (response != null) {
                        System.out.println(response);
                    }
                    parseMenu(readLine());
                    return ACTION_SELECT;
                }
                case "9" -> {
                    logoutQuietly();
                    return ACTION_LOGOUT;
                }
                case "0" -> {
                    if (connected) {
                        sendLine("QUIT");
                        System.out.println(readLine());
                    }
                    return ACTION_QUIT;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void printServiceMenu() {
        System.out.println();
        System.out.println("=== Remote Access Services [" + username + "] ===");
        if (availableServices.contains(SERVICE_WEATHER)) {
            System.out.println("1. Weather Data (US44)");
        }
        if (roles.contains(ROLE_PILOT)) {
            System.out.println("2. Pilot Operations (US86)");
        }else{
            System.out.println("(No remote access services available for your role.)");
        }
        System.out.println("9. Logout");
        System.out.println("0. Quit");
        System.out.print("Option: ");
    }

    private void printWeatherMenu() {
        System.out.println();
        System.out.println("=== Weather Data (US44) [" + username + "] ===");
        final boolean manager = isWeatherManager();
        final boolean consultant = isWeatherConsultant();
        if (manager) {
            System.out.println("1. Register weather data (US041)");
            System.out.println("2. Import bulk weather data (US042)");
        }
        if (consultant) {
            System.out.println("3. Consult weather data (US043)");
        }
        System.out.println("8. Back to services");
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
        username = null;
        roles = new HashSet<>();
        availableServices.clear();
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
    private void pilotOperationsMenu() {

        while (true) {

            System.out.println();
            System.out.println("=== Pilot Operations (US86) [" + username + "] ===");

            System.out.println("1. US080 - Create flight plan -> US075, US076 and US077 not implemented");
            System.out.println("2. US081 - Create flight plan from file -> US075, US076 and US077 not implemented");
            System.out.println("3. US082 - Insert weather data for flight -> US075, US076 and US077 not implemented");
            System.out.println("4. US085 - Validate flight plan -> US075, US076 and US077 not implemented");

            System.out.println("8. Back");
            System.out.print("Option: ");

            final String option = console.nextLine().trim();

            switch (option) {

                case "1" -> System.out.println(
                        "US080 unavailable because US075, US076 and US077 are not implemented."
                );

                case "2" -> System.out.println(
                        "US081 unavailable because US075, US076 and US077 are not implemented."
                );

                case "3" -> System.out.println(
                        "US082 unavailable because US075, US076 and US077 are not implemented."
                );

                case "4" -> System.out.println(
                        "US085 unavailable because US075, US076 and US077 are not implemented."
                );

                case "8" -> {
                    return;
                }

                default -> System.out.println("Invalid option.");
            }
        }
    }
}
