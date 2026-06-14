package pt.isep.sem4pi.presentation.network.remoteaccess;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.controller.aircraft.AddAircraftController;
import pt.isep.sem4pi.application.controller.aircraft.RetireAircraftController;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCompanyFleetController;
import pt.isep.sem4pi.application.controller.route.CreateFlightRouteController;
import pt.isep.sem4pi.application.controller.route.DeactivateFlightRouteController;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class AirTransportCompanyRemoteHandler {

    public static final String CMD_ADD_AIRCRAFT = "ADD_AIRCRAFT";
    public static final String CMD_RETIRE_AIRCRAFT = "RETIRE_AIRCRAFT";
    public static final String CMD_LIST_FLEET = "LIST_FLEET";
    public static final String CMD_CREATE_ROUTE = "CREATE_ROUTE";
    public static final String CMD_DEACTIVATE_ROUTE = "DEACTIVATE_ROUTE";

    private final AuthenticatedSession session;

    public AirTransportCompanyRemoteHandler(final AuthenticatedSession session) {
        this.session = session;
    }

    public boolean dispatch(final String line, final BufferedWriter out) throws IOException {
        if (line.isEmpty()) {
            return true;
        }
        ensureCollaboratorRole();
        final String command = firstToken(line).toUpperCase();
        switch (command) {
            case CMD_ADD_AIRCRAFT:
                handleAddAircraft(line, out);
                return true;
            case CMD_RETIRE_AIRCRAFT:
                handleRetireAircraft(line, out);
                return true;
            case CMD_LIST_FLEET:
                handleListFleet(line, out);
                return true;
            case CMD_CREATE_ROUTE:
                handleCreateRoute(line, out);
                return true;
            case CMD_DEACTIVATE_ROUTE:
                handleDeactivateRoute(line, out);
                return true;
            case RemoteAccessProtocol.CMD_HELP:
                sendHelp(out);
                return true;
            default:
                send(out, RemoteAccessProtocol.ERR + " Unknown ATCC command: " + command);
                return true;
        }
    }

    public void sendHelp(final BufferedWriter out) throws IOException {
        send(out, RemoteAccessProtocol.OK + " ATCC commands (" + RemoteAccessService.ATCC.userStory() + "):");
        send(out, "  ADD_AIRCRAFT <companyIata> <registration> <manufacturer> <model> <country> <eco> <bus> <first>");
        send(out, "  RETIRE_AIRCRAFT <registration>");
        send(out, "  LIST_FLEET [REGISTRATION|MAKER|MODEL|CAPACITY|AGE]");
        send(out, "  CREATE_ROUTE <routeId> <companyIata> <originIcao> <destinationIcao>");
        send(out, "  DEACTIVATE_ROUTE <routeId> <yyyy-MM-dd>");
        send(out, "  BACK | HELP");
    }

    private void handleAddAircraft(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+");
        if (parts.length != 9) {
            send(out, RemoteAccessProtocol.ERR
                    + " Usage: ADD_AIRCRAFT <companyIata> <registration> <manufacturer> <model> <country> <eco> <bus> <first>");
            return;
        }
        try {
            session.runAuthenticated(() -> {
                new AddAircraftController(
                        PersistenceContext.repositories().airTransportCompanies(),
                        PersistenceContext.repositories().aircraftModels()
                ).addAircraft(
                        parts[1], parts[2], parts[3], parts[4], parts[5],
                        Integer.parseInt(parts[6]), Integer.parseInt(parts[7]), Integer.parseInt(parts[8]));
                return null;
            });
            send(out, RemoteAccessProtocol.OK + " Aircraft added successfully.");
        } catch (final NumberFormatException e) {
            send(out, RemoteAccessProtocol.ERR + " Seat counts must be integers.");
        }
    }

    private void handleRetireAircraft(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            send(out, RemoteAccessProtocol.ERR + " Usage: RETIRE_AIRCRAFT <registration>");
            return;
        }
        session.runAuthenticated(() -> {
            new RetireAircraftController().retireAircraft(new AircraftRegistration(parts[1].trim()));
            return null;
        });
        send(out, RemoteAccessProtocol.OK + " Aircraft retired successfully.");
    }

    private void handleListFleet(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+", 2);
        ListCompanyFleetController.FleetSortOption sortOption =
                ListCompanyFleetController.FleetSortOption.REGISTRATION;
        if (parts.length == 2 && !parts[1].isBlank()) {
            try {
                sortOption = ListCompanyFleetController.FleetSortOption.valueOf(parts[1].trim().toUpperCase());
            } catch (final IllegalArgumentException e) {
                send(out, RemoteAccessProtocol.ERR
                        + " Invalid sort option. Use REGISTRATION, MAKER, MODEL, CAPACITY or AGE.");
                return;
            }
        }

        final ListCompanyFleetController.FleetSortOption selectedSort = sortOption;
        final Iterable<Aircraft> fleet = session.runAuthenticated(() ->
                new ListCompanyFleetController().listMyCompanyFleet(selectedSort));

        final java.util.List<Aircraft> aircraftList = new java.util.ArrayList<>();
        fleet.forEach(aircraftList::add);
        send(out, RemoteAccessProtocol.OK + " " + aircraftList.size());
        for (final Aircraft aircraft : aircraftList) {
            sendQuiet(out, formatAircraft(aircraft));
        }
    }

    private void handleCreateRoute(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+");
        if (parts.length != 5) {
            send(out, RemoteAccessProtocol.ERR
                    + " Usage: CREATE_ROUTE <routeId> <companyIata> <originIcao> <destinationIcao>");
            return;
        }
        final FlightRouteDTO dto = new FlightRouteDTO();
        dto.setRouteID(parts[1]);
        dto.setCompanyCode(parts[2]);
        dto.setOriginAirport(parts[3]);
        dto.setDestinationAirport(parts[4]);

        session.runAuthenticated(() -> {
            new CreateFlightRouteController().createFlightRoute(dto);
            return null;
        });
        send(out, RemoteAccessProtocol.OK + " Flight route created successfully.");
    }

    private void handleDeactivateRoute(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+");
        if (parts.length != 3) {
            send(out, RemoteAccessProtocol.ERR + " Usage: DEACTIVATE_ROUTE <routeId> <yyyy-MM-dd>");
            return;
        }
        final LocalDate date;
        try {
            date = LocalDate.parse(parts[2].trim());
        } catch (final DateTimeParseException e) {
            send(out, RemoteAccessProtocol.ERR + " Invalid date format. Use yyyy-MM-dd.");
            return;
        }
        session.runAuthenticated(() -> {
            new DeactivateFlightRouteController().deactivateRoute(parts[1], date);
            return null;
        });
        send(out, RemoteAccessProtocol.OK + " Route deactivated successfully.");
    }

    private void ensureCollaboratorRole() {
        session.ensureAuthenticated();
        session.runAuthenticated(() -> {
            final AuthorizationService authz = AuthzRegistry.authorizationService();
            if (!authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                    && !authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
                throw new SecurityException("Access denied. ATCC role required.");
            }
            return null;
        });
    }

    private static String formatAircraft(final Aircraft aircraft) {
        return String.join(RemoteAccessProtocol.FIELD_SEP,
                aircraft.registration().toString(),
                aircraft.modelId().modelName(),
                aircraft.modelId().manufacturerId().toString(),
                aircraft.status().name(),
                String.valueOf(aircraft.seats().getTotalCapacity()),
                String.valueOf(aircraft.age()));
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
