package pt.isep.sem4pi.presentation.network.remoteaccess;

import pt.isep.sem4pi.application.controller.weatherdata.ConsultWeatherDataController;
import pt.isep.sem4pi.application.controller.weatherdata.ImportWeatherDataController;
import pt.isep.sem4pi.application.controller.weatherdata.RegisterWeatherController;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherImportResultDTO;
import pt.isep.sem4pi.domain.model.weatherdata.WeatherTimeFormatter;
import pt.isep.sem4pi.infrastructure.remoteaccess.RemoteAccessEventType;
import pt.isep.sem4pi.infrastructure.remoteaccess.RemoteAccessLogger;
import pt.isep.sem4pi.presentation.network.weather.WeatherImportSession;
import pt.isep.sem4pi.presentation.network.weather.WeatherProtocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WeatherRemoteHandler {

    private static final int IMPORT_KEEPALIVE_INTERVAL_MS = 15000;
    private static final DateTimeFormatter REGISTER_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AuthenticatedSession session;
    private final RemoteAccessLogger remoteAccessLogger;
    private WeatherImportSession pendingImport;

    public WeatherRemoteHandler(final AuthenticatedSession session) {
        this.session = session;
        this.remoteAccessLogger = RemoteAccessLogger.defaultLogger();
    }

    public boolean hasPendingImport() {
        return pendingImport != null;
    }

    public void clearPendingImport() {
        pendingImport = null;
    }

    public boolean dispatch(final String line, final BufferedWriter out) throws IOException {
        if (line.isEmpty()) {
            return true;
        }
        final String command = firstToken(line).toUpperCase();
        switch (command) {
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
            case RemoteAccessProtocol.CMD_HELP:
                sendHelp(out);
                return true;
            default:
                send(out, RemoteAccessProtocol.ERR + " Unknown weather command: " + command);
                return true;
        }
    }

    public void receiveImportChunk(final String chunk, final BufferedWriter out) throws IOException {
        if (pendingImport == null) {
            send(out, RemoteAccessProtocol.ERR + " No import in progress.");
            return;
        }
        pendingImport.addChunk(chunk);
        if (pendingImport.isComplete()) {
            final WeatherImportSession importSession = pendingImport;
            pendingImport = null;
            try {
                executeImport(importSession.decodeContent(), importSession.getFileName(),
                        importSession.getFileType(), out);
            } catch (final IllegalArgumentException e) {
                send(out, RemoteAccessProtocol.ERR + " Invalid base64 payload.");
            }
        }
    }

    private void handleRegister(final String line, final BufferedWriter out) throws IOException {
        session.ensureAuthenticated();
        final String[] p = line.split("\\s+");
        if (p.length != 11) {
            send(out, RemoteAccessProtocol.ERR
                    + " Usage: REGISTER <areaId> <yyyy-MM-dd> <startHH:mm> <endHH:mm> "
                    + "<temp> <windSpeed> <windDir> <humidity> <pressure> <altitude>");
            return;
        }
        final String areaId = p[1];
        final LocalDate date;
        final LocalTime startTime;
        final LocalTime endTime;
        try {
            date = LocalDate.parse(p[2].trim());
            startTime = LocalTime.parse(p[3].trim(), REGISTER_TIME_FORMAT);
            endTime = LocalTime.parse(p[4].trim(), REGISTER_TIME_FORMAT);
        } catch (final DateTimeParseException e) {
            send(out, RemoteAccessProtocol.ERR + " Invalid date or time format. Use yyyy-MM-dd and HH:mm.");
            return;
        }
        final double temp = Double.parseDouble(p[5]);
        final double windSpeed = Double.parseDouble(p[6]);
        final double windDir = Double.parseDouble(p[7]);
        final double humidity = Double.parseDouble(p[8]);
        final double pressure = Double.parseDouble(p[9]);
        final double altitude = Double.parseDouble(p[10]);

        session.runAuthenticated(() -> new RegisterWeatherController()
                .registerWeather(areaId, date, startTime, endTime, temp, windSpeed, windDir, humidity, pressure, altitude));

        send(out, RemoteAccessProtocol.OK + " Weather data registered for area " + areaId + " on " + date
                + " (" + startTime + " - " + endTime + ").");
        logRemoteAccess(RemoteAccessEventType.REGISTER_WEATHER_DATA);
    }

    private void handleImport(final String line, final BufferedWriter out) throws IOException {
        session.ensureAuthenticated();
        final String[] p = line.split("\\s+", 4);
        if (p.length != 4) {
            send(out, RemoteAccessProtocol.ERR + " Usage: IMPORT <fileName> <fileType|-> <base64Content>");
            return;
        }
        final String fileName = sanitizeFileName(p[1]);
        final String rawType = p[2];
        final String fileType = WeatherProtocol.AUTODETECT.equals(rawType) ? "" : rawType;
        final byte[] content;
        try {
            content = Base64.getDecoder().decode(p[3]);
        } catch (final IllegalArgumentException e) {
            send(out, RemoteAccessProtocol.ERR + " Invalid base64 payload.");
            return;
        }
        executeImport(content, fileName, fileType, out);
    }

    private void handleImportBegin(final String line, final BufferedWriter out) throws IOException {
        session.ensureAuthenticated();
        if (pendingImport != null) {
            send(out, RemoteAccessProtocol.ERR + " Import already in progress.");
            return;
        }
        final String[] p = line.split("\\s+", 4);
        if (p.length != 4) {
            send(out, RemoteAccessProtocol.ERR
                    + " Usage: IMPORT_BEGIN <fileName> <fileType|-> <chunkCount>");
            return;
        }
        final String fileName = sanitizeFileName(p[1]);
        final String fileType = WeatherProtocol.AUTODETECT.equals(p[2]) ? "" : p[2];
        final int chunkCount;
        try {
            chunkCount = Integer.parseInt(p[3]);
        } catch (final NumberFormatException e) {
            send(out, RemoteAccessProtocol.ERR + " Invalid chunk count.");
            return;
        }
        if (chunkCount <= 0 || chunkCount > 100000) {
            send(out, RemoteAccessProtocol.ERR + " Invalid chunk count.");
            return;
        }
        pendingImport = new WeatherImportSession(fileName, fileType, chunkCount);
        send(out, RemoteAccessProtocol.OK + " " + WeatherProtocol.SEND_CHUNKS);
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
            send(out, RemoteAccessProtocol.OK + " " + result.getTotalRecords() + " "
                    + result.getImportedRecords() + " " + result.getRejectedRecords() + " "
                    + errors.size());
            for (final String error : errors) {
                send(out, error);
            }
            logRemoteAccess(RemoteAccessEventType.IMPORT_WEATHER_DATA);
        } finally {
            deleteQuietly(tempFile);
            deleteQuietly(tempDir);
        }
    }

    private void handleConsult(final String line, final BufferedWriter out) throws IOException {
        session.ensureAuthenticated();
        final String[] p = line.split("\\s+");
        if (p.length != 3) {
            send(out, RemoteAccessProtocol.ERR + " Usage: CONSULT <areaId> <yyyy-MM-dd>");
            return;
        }
        final String areaId = p[1];
        final LocalDate date;
        try {
            date = LocalDate.parse(p[2].trim());
        } catch (final DateTimeParseException e) {
            send(out, RemoteAccessProtocol.ERR + " Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        final List<WeatherDataViewDTO> results = session.runAuthenticated(() ->
                new ConsultWeatherDataController().consultByAreaAndDate(areaId, date));

        if (results.isEmpty()) {
            send(out, RemoteAccessProtocol.OK + " 0 " + WeatherProtocol.NO_DATA);
            logRemoteAccess(RemoteAccessEventType.CONSULT_WEATHER_DATA);
            return;
        }

        send(out, RemoteAccessProtocol.OK + " " + results.size());
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
        logRemoteAccess(RemoteAccessEventType.CONSULT_WEATHER_DATA);
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
                        send(out, RemoteAccessProtocol.OK + " " + WeatherProtocol.IMPORT_IN_PROGRESS);
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
            return session.runAuthenticated(importTask);
        } finally {
            done.set(true);
            keepalive.interrupt();
        }
    }

    public void sendHelp(final BufferedWriter out) throws IOException {
        send(out, RemoteAccessProtocol.OK + " Weather commands (" + RemoteAccessService.WEATHER.userStory() + "):");
        send(out, "  REGISTER <areaId> <yyyy-MM-dd> <startHH:mm> <endHH:mm> "
                + "<temp> <windSpeed> <windDir> <humidity> <pressure> <altitude>");
        send(out, "  IMPORT_BEGIN <fileName> <fileType|-> <chunkCount>  (+ base64 chunk lines)");
        send(out, "  IMPORT <fileName> <fileType|-> <base64Content>  (legacy, small files)");
        send(out, "  CONSULT <areaId> <yyyy-MM-dd>");
        send(out, "  BACK | HELP");
    }

    private static String firstToken(final String line) {
        final int idx = line.indexOf(' ');
        return idx < 0 ? line : line.substring(0, idx);
    }

    private static String sanitizeFileName(final String name) {
        final String base = Path.of(name).getFileName().toString();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private void logRemoteAccess(final RemoteAccessEventType eventType) {
        remoteAccessLogger.log(
                session.username(),
                session.clientIp(),
                session.clientPort(),
                RemoteAccessService.WEATHER.userStory(),
                eventType);
    }

    private static void send(final BufferedWriter out, final String message) throws IOException {
        writeLine(out, message);
        RemoteAccessLog.log(">> " + summarizeOutgoing(message));
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
}
