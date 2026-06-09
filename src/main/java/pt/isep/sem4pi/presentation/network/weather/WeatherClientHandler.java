package pt.isep.sem4pi.presentation.network.weather;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;
import pt.isep.sem4pi.application.controller.weatherdata.ConsultWeatherDataController;
import pt.isep.sem4pi.application.controller.weatherdata.ImportWeatherDataController;
import pt.isep.sem4pi.application.controller.weatherdata.RegisterWeatherController;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherTimeFormatter;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WeatherClientHandler implements Runnable {

    private static final Object AUTHZ_LOCK = new Object();
    private static final int IMPORT_KEEPALIVE_INTERVAL_MS = 15000;

    private final Socket socket;

    private String username;
    private String password;
    private boolean authenticated;
    private WeatherImportSession pendingImport;

    public WeatherClientHandler(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        final String remote = socket.getRemoteSocketAddress().toString();
        WeatherServerLog.log("Client connected: " + remote);
        try {
            socket.setKeepAlive(true);
        } catch (final IOException ignored) {
            // best effort
        }
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            send(out, WeatherProtocol.OK + " SEM4PI Weather Remote Access (US44). Type HELP for commands.");

            String line;
            while ((line = in.readLine()) != null) {
                final String trimmed = line.trim();
                if (pendingImport != null) {
                    WeatherServerLog.log("<< " + remote + " IMPORT_CHUNK "
                            + pendingImport.getReceivedChunks() + 1 + "/"
                            + pendingImport.getExpectedChunks());
                    receiveImportChunk(trimmed, out);
                    continue;
                }
                WeatherServerLog.log("<< " + remote + " " + WeatherServerLog.summarizeIncoming(trimmed));
                final boolean keepGoing = dispatch(trimmed, out);
                if (!keepGoing) {
                    break;
                }
            }
        } catch (final IOException e) {
            WeatherServerLog.log("Connection error (" + remote + "): " + e.getMessage());
        } finally {
            closeQuietly();
            WeatherServerLog.log("Client disconnected: " + remote);
        }
    }

    private boolean dispatch(final String line, final BufferedWriter out) throws IOException {
        if (line.isEmpty()) {
            return true;
        }
        final String command = firstToken(line).toUpperCase();
        try {
            switch (command) {
                case WeatherProtocol.CMD_HELLO:
                    send(out, WeatherProtocol.OK + " SEM4PI Weather Remote Access ready.");
                    return true;
                case WeatherProtocol.CMD_HELP:
                    sendHelp(out);
                    return true;
                case WeatherProtocol.CMD_LOGIN:
                    handleLogin(line, out);
                    return true;
                case WeatherProtocol.CMD_LOGOUT:
                    this.authenticated = false;
                    this.username = null;
                    this.password = null;
                    this.pendingImport = null;
                    send(out, WeatherProtocol.OK + " Logged out.");
                    return true;
                case WeatherProtocol.CMD_REGISTER:
                    handleRegister(line, out);
                    return true;
                case WeatherProtocol.CMD_IMPORT:
                    handleImport(line, out);
                    return true;
                case WeatherProtocol.CMD_IMPORT_BEGIN:
                    handleImportBegin(line, out);
                    return true;
                case WeatherProtocol.CMD_CONSULT:
                    handleConsult(line, out);
                    return true;
                case WeatherProtocol.CMD_QUIT:
                    send(out, WeatherProtocol.OK + " Bye.");
                    return false;
                default:
                    send(out, WeatherProtocol.ERR + " Unknown command: " + command);
                    return true;
            }
        } catch (final SecurityException e) {
            send(out, WeatherProtocol.ERR + " " + e.getMessage());
            return true;
        } catch (final IllegalArgumentException e) {
            send(out, WeatherProtocol.ERR + " " + e.getMessage());
            return true;
        } catch (final RuntimeException e) {
            send(out, WeatherProtocol.ERR + " Unexpected error: " + e.getMessage());
            return true;
        }
    }

    private void handleLogin(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+", 3);
        if (parts.length < 3) {
            send(out, WeatherProtocol.ERR + " Usage: LOGIN <username> <password>");
            return;
        }
        final String user = parts[1];
        final String pass = parts[2];

        final String roles;
        synchronized (AUTHZ_LOCK) {
            final AuthorizationService authz = AuthzRegistry.authorizationService();
            try {
                final AuthenticationController.LoginResult result =
                        new AuthenticationController().login(user, pass);
                if (result != AuthenticationController.LoginResult.SUCCESS) {
                    send(out, WeatherProtocol.ERR + " Invalid credentials or access denied.");
                    return;
                }
                roles = collectRoles(authz);
            } finally {
                authz.clearSession();
            }
        }

        this.username = user;
        this.password = pass;
        this.authenticated = true;
        send(out, WeatherProtocol.OK + " " + user + " " + roles);
    }

    private static String collectRoles(final AuthorizationService authz) {
        final java.util.List<String> roles = new java.util.ArrayList<>();
        if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            roles.add("ADMIN");
        }
        if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.WEATHER_PERSON)) {
            roles.add("WEATHER_PERSON");
        }
        if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.PILOT)) {
            roles.add("PILOT");
        }
        if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.FLIGHT_CONTROL_OPERATOR)) {
            roles.add("FLIGHT_CONTROL_OPERATOR");
        }
        if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) {
            roles.add("AIR_TRANSPORT_COMPANY_COLLABORATOR");
        }
        if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.BACKOFFICE_OPERATOR)) {
            roles.add("BACKOFFICE_OPERATOR");
        }
        return roles.isEmpty() ? "-" : String.join(",", roles);
    }

    private void handleRegister(final String line, final BufferedWriter out) throws IOException {
        ensureAuthenticated();
        final String[] p = line.split("\\s+");
        if (p.length != 9) {
            send(out, WeatherProtocol.ERR
                    + " Usage: REGISTER <areaId> <yyyy-MM-dd> <temp> <windSpeed> <windDir> <humidity> <pressure> <altitude>");
            return;
        }
        final String areaId = p[1];
        final LocalDate date;
        try {
            date = LocalDate.parse(p[2].trim());
        } catch (final DateTimeParseException e) {
            send(out, WeatherProtocol.ERR + " Invalid date format. Use yyyy-MM-dd.");
            return;
        }
        final double temp = Double.parseDouble(p[3]);
        final double windSpeed = Double.parseDouble(p[4]);
        final double windDir = Double.parseDouble(p[5]);
        final double humidity = Double.parseDouble(p[6]);
        final double pressure = Double.parseDouble(p[7]);
        final double altitude = Double.parseDouble(p[8]);

        runAuthenticated(() -> new RegisterWeatherController()
                .registerWeather(areaId, date, temp, windSpeed, windDir, humidity, pressure, altitude));

        send(out, WeatherProtocol.OK + " Weather data registered for area " + areaId + " on " + date + ".");
    }

    private void handleImport(final String line, final BufferedWriter out) throws IOException {
        ensureAuthenticated();
        final String[] p = line.split("\\s+", 4);
        if (p.length != 4) {
            send(out, WeatherProtocol.ERR + " Usage: IMPORT <fileName> <fileType|-> <base64Content>");
            return;
        }
        final String fileName = sanitizeFileName(p[1]);
        final String rawType = p[2];
        final String fileType = WeatherProtocol.AUTODETECT.equals(rawType) ? "" : rawType;
        final byte[] content;
        try {
            content = Base64.getDecoder().decode(p[3]);
        } catch (final IllegalArgumentException e) {
            send(out, WeatherProtocol.ERR + " Invalid base64 payload.");
            return;
        }
        executeImport(content, fileName, fileType, out);
    }

    private void handleImportBegin(final String line, final BufferedWriter out) throws IOException {
        ensureAuthenticated();
        if (pendingImport != null) {
            send(out, WeatherProtocol.ERR + " Import already in progress.");
            return;
        }
        final String[] p = line.split("\\s+", 4);
        if (p.length != 4) {
            send(out, WeatherProtocol.ERR
                    + " Usage: IMPORT_BEGIN <fileName> <fileType|-> <chunkCount>");
            return;
        }
        final String fileName = sanitizeFileName(p[1]);
        final String fileType = WeatherProtocol.AUTODETECT.equals(p[2]) ? "" : p[2];
        final int chunkCount;
        try {
            chunkCount = Integer.parseInt(p[3]);
        } catch (final NumberFormatException e) {
            send(out, WeatherProtocol.ERR + " Invalid chunk count.");
            return;
        }
        if (chunkCount <= 0 || chunkCount > 100000) {
            send(out, WeatherProtocol.ERR + " Invalid chunk count.");
            return;
        }
        pendingImport = new WeatherImportSession(fileName, fileType, chunkCount);
        send(out, WeatherProtocol.OK + " " + WeatherProtocol.SEND_CHUNKS);
    }

    private void receiveImportChunk(final String chunk, final BufferedWriter out) throws IOException {
        if (pendingImport == null) {
            send(out, WeatherProtocol.ERR + " No import in progress.");
            return;
        }
        pendingImport.addChunk(chunk);
        if (pendingImport.isComplete()) {
            final WeatherImportSession session = pendingImport;
            pendingImport = null;
            try {
                executeImport(session.decodeContent(), session.getFileName(), session.getFileType(), out);
            } catch (final IllegalArgumentException e) {
                send(out, WeatherProtocol.ERR + " Invalid base64 payload.");
            }
        }
    }

    private void executeImport(
            final byte[] content,
            final String fileName,
            final String fileType,
            final BufferedWriter out) throws IOException {
        Path tempDir = null;
        Path tempFile = null;
        try {
            tempDir = Files.createTempDirectory("weather-upload-");
            tempFile = tempDir.resolve(fileName);
            Files.write(tempFile, content);

            final String pathForImport = tempFile.toString();
            final WeatherImportResultDTO result = runImportWithKeepalive(out, () ->
                    new ImportWeatherDataController().importWeatherData(pathForImport, fileType));

            final List<String> errors = result.getErrors();
            send(out, WeatherProtocol.OK + " " + result.getTotalRecords() + " "
                    + result.getImportedRecords() + " " + result.getRejectedRecords() + " "
                    + errors.size());
            for (final String error : errors) {
                send(out, error);
            }
        } finally {
            deleteQuietly(tempFile);
            deleteQuietly(tempDir);
        }
    }

    private void handleConsult(final String line, final BufferedWriter out) throws IOException {
        ensureAuthenticated();
        final String[] p = line.split("\\s+");
        if (p.length != 3) {
            send(out, WeatherProtocol.ERR + " Usage: CONSULT <areaId> <yyyy-MM-dd>");
            return;
        }
        final String areaId = p[1];
        final LocalDate date;
        try {
            date = LocalDate.parse(p[2].trim());
        } catch (final DateTimeParseException e) {
            send(out, WeatherProtocol.ERR + " Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        final List<WeatherDataViewDTO> results = runAuthenticated(() ->
                new ConsultWeatherDataController().consultByAreaAndDate(areaId, date));

        if (results.isEmpty()) {
            send(out, WeatherProtocol.OK + " 0 " + WeatherProtocol.NO_DATA);
            return;
        }

        send(out, WeatherProtocol.OK + " " + results.size());
        for (final WeatherDataViewDTO dto : results) {
            sendQuiet(out, String.join(WeatherProtocol.FIELD_SEP,
                    String.valueOf(dto.getDate()),
                    WeatherTimeFormatter.format(dto.getStartTime()),
                    WeatherTimeFormatter.format(dto.getEndTime()),
                    String.valueOf(dto.getTemperatureCelsius()),
                    String.valueOf(dto.getWindSpeedKnots()),
                    String.valueOf(dto.getWindDirectionDegrees()),
                    String.valueOf(dto.getPressureHpa()),
                    String.valueOf(dto.getHumidityPercentage()),
                    String.valueOf(dto.getAltitudeMeters())));
        }
    }

    private WeatherImportResultDTO runImportWithKeepalive(
            final BufferedWriter out,
            final Callable<WeatherImportResultDTO> importTask) throws IOException {
        final AtomicBoolean done = new AtomicBoolean(false);
        final Thread keepalive = new Thread(() -> {
            while (!done.get()) {
                try {
                    Thread.sleep(IMPORT_KEEPALIVE_INTERVAL_MS);
                    if (!done.get()) {
                        send(out, WeatherProtocol.OK + " " + WeatherProtocol.IMPORT_IN_PROGRESS);
                    }
                } catch (final InterruptedException e) {
                    break;
                } catch (final IOException e) {
                    break;
                }
            }
        }, "weather-import-keepalive");
        keepalive.setDaemon(true);
        keepalive.start();
        try {
            return runAuthenticated(importTask);
        } finally {
            done.set(true);
            keepalive.interrupt();
        }
    }

    private <T> T runAuthenticated(final Callable<T> action) {
        ensureAuthenticated();
        synchronized (AUTHZ_LOCK) {
            final AuthorizationService authz = AuthzRegistry.authorizationService();
            final AuthenticationController.LoginResult result =
                    new AuthenticationController().login(username, password);
            if (result != AuthenticationController.LoginResult.SUCCESS) {
                this.authenticated = false;
                throw new SecurityException("Session expired or access revoked. Please LOGIN again.");
            }
            try {
                return action.call();
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                authz.clearSession();
            }
        }
    }

    private void ensureAuthenticated() {
        if (!authenticated) {
            throw new SecurityException("Authentication required. Use LOGIN <username> <password>.");
        }
    }

    private void sendHelp(final BufferedWriter out) throws IOException {
        send(out, WeatherProtocol.OK + " Available commands:");
        send(out, "  LOGIN <username> <password>");
        send(out, "  REGISTER <areaId> <yyyy-MM-dd> <temp> <windSpeed> <windDir> <humidity> <pressure> <altitude>");
        send(out, "  IMPORT_BEGIN <fileName> <fileType|-> <chunkCount>  (+ base64 chunk lines)");
        send(out, "  IMPORT <fileName> <fileType|-> <base64Content>  (legacy, small files)");
        send(out, "  CONSULT <areaId> <yyyy-MM-dd>");
        send(out, "  LOGOUT | QUIT | HELP");
    }

    private static String firstToken(final String line) {
        final int idx = line.indexOf(' ');
        return idx < 0 ? line : line.substring(0, idx);
    }

    private static String sanitizeFileName(final String name) {
        final String base = Path.of(name).getFileName().toString();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static void send(final BufferedWriter out, final String message) throws IOException {
        writeLine(out, message);
        WeatherServerLog.log(">> " + summarizeOutgoing(message));
    }

    private static void sendQuiet(final BufferedWriter out, final String message) throws IOException {
        writeLine(out, message);
    }

    private static void writeLine(final BufferedWriter out, final String message) throws IOException {
        synchronized (out) {
            out.write(message);
            out.write('\n');
            out.flush();
        }
    }

    private static String summarizeOutgoing(final String message) {
        if (message == null) {
            return "";
        }
        if (message.contains(WeatherProtocol.IMPORT_IN_PROGRESS)) {
            return message;
        }
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        return message;
    }

    private static void deleteQuietly(final Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (final IOException ignored) {
            // best effort cleanup
        }
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (final IOException ignored) {
            // already closing
        }
    }
}
