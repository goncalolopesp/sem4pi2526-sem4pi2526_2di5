package pt.isep.sem4pi.presentation.network.remoteaccess;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.controller.flightplan.AssociateWeatherDataController;
import pt.isep.sem4pi.application.controller.flightplan.CreateFlightPlanController;
import pt.isep.sem4pi.application.controller.flightplan.ImportFlightPlanController;
import pt.isep.sem4pi.application.controller.flightplan.TestFlightPlanController;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.application.service.flightplan.WeatherAssociationResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class PilotRemoteHandler {

    public static final String CMD_LIST_ROUTES = "LIST_ROUTES";
    public static final String CMD_LIST_FLIGHTS = "LIST_FLIGHTS";
    public static final String CMD_LIST_MY_PLANS = "LIST_MY_PLANS";
    public static final String CMD_CREATE_FLIGHT_PLAN = "CREATE_FLIGHT_PLAN";
    public static final String CMD_IMPORT_PLAN_BEGIN = "IMPORT_PLAN_BEGIN";
    public static final String CMD_LIST_WEATHER = "LIST_WEATHER";
    public static final String CMD_ASSOCIATE_WEATHER = "ASSOCIATE_WEATHER";
    public static final String CMD_LIST_DRAFT_PLANS = "LIST_DRAFT_PLANS";
    public static final String CMD_TEST_FLIGHT_PLAN = "TEST_FLIGHT_PLAN";

    private final AuthenticatedSession session;
    private FlightPlanImportSession pendingImport;

    public PilotRemoteHandler(final AuthenticatedSession session) {
        this.session = session;
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
        ensurePilotRole();
        final String command = firstToken(line).toUpperCase();
        switch (command) {
            case CMD_LIST_ROUTES:
                handleListRoutes(out);
                return true;
            case CMD_LIST_FLIGHTS:
                handleListFlights(out);
                return true;
            case CMD_LIST_MY_PLANS:
                handleListMyPlans(out);
                return true;
            case CMD_CREATE_FLIGHT_PLAN:
                handleCreateFlightPlan(line, out);
                return true;
            case CMD_IMPORT_PLAN_BEGIN:
                handleImportPlanBegin(line, out);
                return true;
            case CMD_LIST_WEATHER:
                handleListWeather(out);
                return true;
            case CMD_ASSOCIATE_WEATHER:
                handleAssociateWeather(line, out);
                return true;
            case CMD_LIST_DRAFT_PLANS:
                handleListDraftPlans(out);
                return true;
            case CMD_TEST_FLIGHT_PLAN:
                handleTestFlightPlan(line, out);
                return true;
            case RemoteAccessProtocol.CMD_HELP:
                sendHelp(out);
                return true;
            default:
                send(out, RemoteAccessProtocol.ERR + " Unknown pilot command: " + command);
                return true;
        }
    }

    public void receiveImportLine(final String line, final BufferedWriter out) throws IOException {
        if (pendingImport == null) {
            send(out, RemoteAccessProtocol.ERR + " No flight plan import in progress.");
            return;
        }
        pendingImport.addLine(line);
        if (!pendingImport.isComplete()) {
            return;
        }
        final FlightPlanImportSession completed = pendingImport;
        pendingImport = null;
        executeImport(completed.content(), out);
    }

    public void sendHelp(final BufferedWriter out) throws IOException {
        send(out, RemoteAccessProtocol.OK + " Pilot commands (" + RemoteAccessService.PILOT.userStory() + "):");
        send(out, "  LIST_ROUTES");
        send(out, "  LIST_FLIGHTS");
        send(out, "  LIST_MY_PLANS");
        send(out, "  CREATE_FLIGHT_PLAN <routeId> <flightId> <REGULAR|CHARTER> <pilotId>"
                + " <depDay> <depMonth> <depYear> <depH> <depM> <depS>"
                + " <arrH> <arrM> <arrS> <fuelQty> <KG|L>");
        send(out, "  IMPORT_PLAN_BEGIN <lineCount>  (then send DSL lines)");
        send(out, "  LIST_WEATHER");
        send(out, "  ASSOCIATE_WEATHER <planId> <weatherId[,weatherId...]>");
        send(out, "  LIST_DRAFT_PLANS");
        send(out, "  TEST_FLIGHT_PLAN <planId>");
        send(out, "  BACK | HELP");
    }

    private void handleListRoutes(final BufferedWriter out) throws IOException {
        final List<FlightRoute> routes = new ArrayList<>();
        session.runAuthenticated(() -> {
            new CreateFlightPlanController().getRoutes().forEach(routes::add);
            return null;
        });
        send(out, RemoteAccessProtocol.OK + " " + routes.size());
        for (final FlightRoute route : routes) {
            sendQuiet(out, String.join(RemoteAccessProtocol.FIELD_SEP,
                    route.identity().code(),
                    route.originAirport().code(),
                    route.destinationAirport().code(),
                    route.companyCode().code()));
        }
    }

    private void handleListFlights(final BufferedWriter out) throws IOException {
        final List<Flight> flights = session.runAuthenticated(() -> new CreateFlightPlanController().getFlights());
        send(out, RemoteAccessProtocol.OK + " " + flights.size());
        for (final Flight flight : flights) {
            sendQuiet(out, String.join(RemoteAccessProtocol.FIELD_SEP,
                    flight.identity().value(),
                    flight.aircraftRegistration().value(),
                    flight.status().name()));
        }
    }

    private void handleListMyPlans(final BufferedWriter out) throws IOException {
        final List<FlightPlan> plans = session.runAuthenticated(() -> new AssociateWeatherDataController().myFlightPlans());
        send(out, RemoteAccessProtocol.OK + " " + plans.size());
        for (final FlightPlan plan : plans) {
            sendQuiet(out, String.join(RemoteAccessProtocol.FIELD_SEP,
                    plan.identity().value(),
                    plan.flightId().value(),
                    plan.status().name()));
        }
    }

    private void handleCreateFlightPlan(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+");
        if (parts.length != 16) {
            send(out, RemoteAccessProtocol.ERR
                    + " Usage: CREATE_FLIGHT_PLAN <routeId> <flightId> <REGULAR|CHARTER> <pilotId>"
                    + " <depDay> <depMonth> <depYear> <depH> <depM> <depS>"
                    + " <arrH> <arrM> <arrS> <fuelQty> <KG|L>");
            return;
        }
        try {
            final String routeId = parts[1];
            final FlightPlan plan = session.runAuthenticated(() -> {
                final FlightRoute route = findRoute(routeId);
                final CreateFlightPlanLegDTO leg = new CreateFlightPlanLegDTO(
                        route.originAirport().code(),
                        Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6]),
                        Integer.parseInt(parts[7]),
                        Integer.parseInt(parts[8]),
                        Integer.parseInt(parts[9]),
                        Integer.parseInt(parts[10]),
                        route.destinationAirport().code(),
                        Integer.parseInt(parts[11]),
                        Integer.parseInt(parts[12]),
                        Integer.parseInt(parts[13]),
                        Double.parseDouble(parts[14]),
                        parts[15],
                        null);
                final CreateFlightPlanDTO dto = new CreateFlightPlanDTO(
                        null,
                        routeId,
                        parts[2],
                        parts[3],
                        null,
                        parts[4],
                        List.of(leg));
                return new CreateFlightPlanController().createFlightPlan(dto);
            });
            send(out, RemoteAccessProtocol.OK + " Flight plan " + plan.identity().value()
                    + " created with status " + plan.status() + ".");
        } catch (final NumberFormatException e) {
            send(out, RemoteAccessProtocol.ERR + " Numeric fields must be valid numbers.");
        }
    }

    private FlightRoute findRoute(final String routeId) {
        for (final FlightRoute route : new CreateFlightPlanController().getRoutes()) {
            if (route.identity().code().equals(routeId)) {
                return route;
            }
        }
        throw new IllegalArgumentException("Route not found: " + routeId);
    }

    private void handleImportPlanBegin(final String line, final BufferedWriter out) throws IOException {
        if (pendingImport != null) {
            send(out, RemoteAccessProtocol.ERR + " Import already in progress.");
            return;
        }
        final String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            send(out, RemoteAccessProtocol.ERR + " Usage: IMPORT_PLAN_BEGIN <lineCount>");
            return;
        }
        final int lineCount;
        try {
            lineCount = Integer.parseInt(parts[1]);
        } catch (final NumberFormatException e) {
            send(out, RemoteAccessProtocol.ERR + " Invalid line count.");
            return;
        }
        if (lineCount <= 0 || lineCount > 10000) {
            send(out, RemoteAccessProtocol.ERR + " Invalid line count.");
            return;
        }
        pendingImport = new FlightPlanImportSession(lineCount);
        send(out, RemoteAccessProtocol.OK + " " + FlightPlanImportSession.SEND_LINES);
    }

    private void executeImport(final String content, final BufferedWriter out) throws IOException {
        Path tempDir = null;
        Path tempFile = null;
        try {
            tempDir = Files.createTempDirectory("flight-plan-upload-");
            tempFile = tempDir.resolve("import.dsldig5");
            Files.writeString(tempFile, content, StandardCharsets.UTF_8);
            final String path = tempFile.toString();
            final FlightPlanImportResultDTO result = session.runAuthenticated(() ->
                    new ImportFlightPlanController().importFlightPlan(path));
            if (result.isSuccess()) {
                send(out, RemoteAccessProtocol.OK + " Flight plan imported: " + result.getFlightPlanId());
            } else {
                send(out, RemoteAccessProtocol.ERR + " Import failed (" + result.getErrors().size() + " errors)");
                for (final String error : result.getErrors()) {
                    sendQuiet(out, error);
                }
            }
        } catch (final IOException e) {
            send(out, RemoteAccessProtocol.ERR + " Could not process import: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (final IOException ignored) {
                    // best effort
                }
            }
            if (tempDir != null) {
                try {
                    Files.deleteIfExists(tempDir);
                } catch (final IOException ignored) {
                    // best effort
                }
            }
        }
    }

    private void handleListWeather(final BufferedWriter out) throws IOException {
        final List<WeatherDataViewDTO> weather = session.runAuthenticated(() ->
                new AssociateWeatherDataController().weatherData());
        send(out, RemoteAccessProtocol.OK + " " + weather.size());
        for (final WeatherDataViewDTO data : weather) {
            sendQuiet(out, String.join(RemoteAccessProtocol.FIELD_SEP,
                    String.valueOf(data.getWeatherDataId()),
                    data.getAirControlAreaId(),
                    data.getDate().toString(),
                    data.getStartTime().toString(),
                    data.getEndTime().toString()));
        }
    }

    private void handleAssociateWeather(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+", 3);
        if (parts.length != 3 || parts[2].isBlank()) {
            send(out, RemoteAccessProtocol.ERR + " Usage: ASSOCIATE_WEATHER <planId> <weatherId[,weatherId...]>");
            return;
        }
        final String planId = parts[1];
        final List<Long> weatherIds = Arrays.stream(parts[2].split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
        try {
            final WeatherAssociationResult result = session.runAuthenticated(() ->
                    new AssociateWeatherDataController().associate(planId, weatherIds));
            send(out, RemoteAccessProtocol.OK + " Associated " + result.associatedWeatherCount()
                    + " weather record(s) with plan " + result.flightPlan().identity().value()
                    + (result.previousTestInvalidated() ? " TEST_INVALIDATED" : ""));
        } catch (final NumberFormatException e) {
            send(out, RemoteAccessProtocol.ERR + " Weather ids must be numeric.");
        }
    }

    private void handleListDraftPlans(final BufferedWriter out) throws IOException {
        final List<FlightPlan> plans = session.runAuthenticated(() ->
                new TestFlightPlanController().listDraftFlightPlans());
        send(out, RemoteAccessProtocol.OK + " " + plans.size());
        for (final FlightPlan plan : plans) {
            sendQuiet(out, String.join(RemoteAccessProtocol.FIELD_SEP,
                    plan.identity().value(),
                    plan.flightId().value(),
                    plan.status().name()));
        }
    }

    private void handleTestFlightPlan(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            send(out, RemoteAccessProtocol.ERR + " Usage: TEST_FLIGHT_PLAN <planId>");
            return;
        }
        try {
            final TestFlightPlanResultDTO result = session.runAuthenticated(() ->
                    new TestFlightPlanController().testFlightPlan(parts[1].trim()));
            send(out, RemoteAccessProtocol.OK + " " + result.flightPlanId()
                    + " " + (result.accepted() ? "VALIDATED" : "REJECTED")
                    + " " + sanitize(result.message()));
        } catch (final IOException e) {
            send(out, RemoteAccessProtocol.ERR + " Test failed: " + e.getMessage());
        }
    }

    private void ensurePilotRole() {
        session.ensureAuthenticated();
        session.runAuthenticated(() -> {
            final AuthorizationService authz = AuthzRegistry.authorizationService();
            if (!authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.PILOT)
                    && !authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
                throw new SecurityException("Access denied. PILOT role required.");
            }
            return null;
        });
    }

    private static String sanitize(final String message) {
        if (message == null) {
            return "";
        }
        return message.replace('\n', ' ').trim();
    }

    private static String firstToken(final String line) {
        final int idx = line.indexOf(' ');
        return idx < 0 ? line : line.substring(0, idx);
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
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        return message;
    }
}
