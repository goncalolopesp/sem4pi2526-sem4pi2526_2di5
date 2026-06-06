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
import java.util.Base64;
import java.util.Scanner;

public final class WeatherClientApp {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8044;
    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Scanner console = new Scanner(System.in);

    private WeatherClientApp(final BufferedReader in, final BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    public static void main(final String[] args) {
        final String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("Connecting to weather server at " + host + ":" + port + " ...");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
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
        boolean exit = false;
        while (!exit) {
            printMenu();
            final String option = console.nextLine().trim();
            switch (option) {
                case "1" -> login();
                case "2" -> registerWeather();
                case "3" -> importWeather();
                case "4" -> consultWeather();
                case "5" -> logout();
                case "0" -> {
                    sendLine("QUIT");
                    System.out.println(readLine());
                    exit = true;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Weather Person Remote Access ===");
        System.out.println("1. Login");
        System.out.println("2. Register weather data (US041)");
        System.out.println("3. Import bulk weather data (US042)");
        System.out.println("4. Consult weather data (US043)");
        System.out.println("5. Logout");
        System.out.println("0. Quit");
        System.out.print("Option: ");
    }

    private void login() throws IOException {
        System.out.print("Username: ");
        final String user = console.nextLine().trim();
        System.out.print("Password: ");
        final String pass = console.nextLine();
        sendLine("LOGIN " + user + " " + pass);
        System.out.println(readLine());
    }

    private void logout() throws IOException {
        sendLine("LOGOUT");
        System.out.println(readLine());
    }

    private void registerWeather() throws IOException {
        System.out.print("Air Control Area ID: ");
        final String areaId = console.nextLine().trim();
        final String temp = ask("Temperature (Celsius)");
        final String windSpeed = ask("Wind speed (knots)");
        final String windDir = ask("Wind direction (0-359)");
        final String humidity = ask("Humidity (%)");
        final String pressure = ask("Pressure (hPa)");
        final String altitude = ask("Altitude (meters)");

        sendLine(String.join(" ", "REGISTER", areaId, temp, windSpeed, windDir,
                humidity, pressure, altitude));
        System.out.println(readLine());
    }

    private void importWeather() throws IOException {
        System.out.print("Local file path (csv/xlsx): ");
        final String filePath = console.nextLine().trim();
        final Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            System.out.println("File not found: " + filePath);
            return;
        }
        System.out.print("File type (csv/xlsx, blank to auto-detect): ");
        final String fileType = console.nextLine().trim();

        final byte[] content;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
            return;
        }
        final String base64 = Base64.getEncoder().encodeToString(content);
        final String type = fileType.isEmpty() ? "-" : fileType;
        sendLine("IMPORT " + path.getFileName() + " " + type + " " + base64);

        final String header = readLine();
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
        if (header == null || !header.startsWith("OK")) {
            System.out.println(header);
            return;
        }
        final String[] parts = header.split("\\s+");
        final int count = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
        if (count == 0) {
            System.out.println("No weather data found.");
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
    }

    private String ask(final String label) {
        System.out.print(label + ": ");
        return console.nextLine().trim();
    }

    private void sendLine(final String message) throws IOException {
        out.write(message);
        out.write('\n');
        out.flush();
    }

    private String readLine() throws IOException {
        return in.readLine();
    }
}
