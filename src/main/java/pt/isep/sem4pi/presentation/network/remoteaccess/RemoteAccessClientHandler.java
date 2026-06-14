package pt.isep.sem4pi.presentation.network.remoteaccess;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.remoteaccess.RemoteAccessEventType;
import pt.isep.sem4pi.infrastructure.remoteaccess.RemoteAccessLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public final class RemoteAccessClientHandler implements Runnable, AuthenticatedSession {

    private static final Object AUTHZ_LOCK = new Object();

    private final Socket socket;
    private final RemoteAccessLogger remoteAccessLogger;

    private String username;
    private String password;
    private boolean authenticated;
    private Set<String> roles = Set.of();
    private List<RemoteAccessService> availableServices = List.of();
    private RemoteAccessService selectedService;
    private WeatherRemoteHandler weatherHandler;
    private AirTransportCompanyRemoteHandler airTransportHandler;

    public RemoteAccessClientHandler(final Socket socket) {
        this.socket = socket;
        this.remoteAccessLogger = RemoteAccessLogger.defaultLogger();
    }

    @Override
    public void run() {
        final String remote = socket.getRemoteSocketAddress().toString();
        RemoteAccessLog.log("Client connected: " + remote);
        try {
            socket.setKeepAlive(true);
        } catch (final IOException ignored) {
            // best effort
        }
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            send(out, RemoteAccessProtocol.OK
                    + " SEM4PI Remote Access. Please LOGIN to continue. Type HELP for commands.");

            String line;
            while ((line = in.readLine()) != null) {
                final String trimmed = line.trim();
                if (weatherHandler != null && weatherHandler.hasPendingImport()) {
                    RemoteAccessLog.log("<< " + remote + " IMPORT_CHUNK");
                    weatherHandler.receiveImportChunk(trimmed, out);
                    continue;
                }
                RemoteAccessLog.log("<< " + remote + " " + RemoteAccessLog.summarizeIncoming(trimmed));
                final boolean keepGoing = dispatch(trimmed, out);
                if (!keepGoing) {
                    break;
                }
            }
        } catch (final IOException e) {
            RemoteAccessLog.log("Connection error (" + remote + "): " + e.getMessage());
        } finally {
            logRemoteAccess(currentUsername(), RemoteAccessEventType.DISCONNECT);
            closeQuietly();
            RemoteAccessLog.log("Client disconnected: " + remote);
        }
    }

    private boolean dispatch(final String line, final BufferedWriter out) throws IOException {
        if (line.isEmpty()) {
            return true;
        }
        final String command = firstToken(line).toUpperCase();
        try {
            if (selectedService == RemoteAccessService.WEATHER) {
                if (RemoteAccessProtocol.CMD_BACK.equals(command)) {
                    return handleBack(out);
                }
                if (isGlobalCommand(command)) {
                    return dispatchGlobal(line, command, out);
                }
                return weatherHandler.dispatch(line, out);
            }
            if (selectedService == RemoteAccessService.ATCC) {
                if (RemoteAccessProtocol.CMD_BACK.equals(command)) {
                    return handleBack(out);
                }
                if (isGlobalCommand(command)) {
                    return dispatchGlobal(line, command, out);
                }
                return airTransportHandler.dispatch(line, out);
            }

            return dispatchGlobal(line, command, out);
        } catch (final SecurityException e) {
            sendSafe(out, RemoteAccessProtocol.ERR + " " + e.getMessage());
            return true;
        } catch (final IllegalArgumentException e) {
            sendSafe(out, RemoteAccessProtocol.ERR + " " + e.getMessage());
            return true;
        } catch (final RuntimeException e) {
            sendSafe(out, RemoteAccessProtocol.ERR + " Unexpected error: " + e.getMessage());
            return true;
        } catch (final IOException e) {
            RemoteAccessLog.log("Failed to write response: " + e.getMessage());
            return false;
        }
    }

    private boolean dispatchGlobal(final String line, final String command, final BufferedWriter out)
            throws IOException {
        switch (command) {
            case RemoteAccessProtocol.CMD_HELLO:
                send(out, RemoteAccessProtocol.OK + " SEM4PI Remote Access ready.");
                return true;
            case RemoteAccessProtocol.CMD_HELP:
                sendHelp(out);
                return true;
            case RemoteAccessProtocol.CMD_LOGIN:
                handleLogin(line, out);
                return true;
            case RemoteAccessProtocol.CMD_LOGOUT:
                handleLogout(out);
                return true;
            case RemoteAccessProtocol.CMD_MENU:
                handleMenu(out);
                return true;
            case RemoteAccessProtocol.CMD_SELECT:
                handleSelect(line, out);
                return true;
            case RemoteAccessProtocol.CMD_QUIT:
                send(out, RemoteAccessProtocol.OK + " Bye.");
                return false;
            default:
                if (authenticated) {
                    send(out, RemoteAccessProtocol.ERR
                            + " Select a remote access service first. Use SELECT <service> or MENU.");
                } else {
                    send(out, RemoteAccessProtocol.ERR + " Authentication required. Use LOGIN <username> <password>.");
                }
                return true;
        }
    }

    private boolean isGlobalCommand(final String command) {
        return RemoteAccessProtocol.CMD_HELP.equals(command)
                || RemoteAccessProtocol.CMD_LOGOUT.equals(command)
                || RemoteAccessProtocol.CMD_MENU.equals(command)
                || RemoteAccessProtocol.CMD_SELECT.equals(command)
                || RemoteAccessProtocol.CMD_QUIT.equals(command);
    }

    private void handleLogin(final String line, final BufferedWriter out) throws IOException {
        final String[] parts = line.split("\\s+", 3);
        if (parts.length < 3) {
            send(out, RemoteAccessProtocol.ERR + " Usage: LOGIN <username> <password>");
            logRemoteAccess(parts.length > 1 ? parts[1] : "-", RemoteAccessEventType.LOGIN_FAILED);
            return;
        }
        final String user = parts[1];
        final String pass = parts[2];

        final String roleList;
        synchronized (AUTHZ_LOCK) {
            final AuthorizationService authz = AuthzRegistry.authorizationService();
            try {
                final AuthenticationController.LoginResult result =
                        new AuthenticationController().login(user, pass);
                if (result != AuthenticationController.LoginResult.SUCCESS) {
                    send(out, RemoteAccessProtocol.ERR + " Invalid credentials or access denied.");
                    logRemoteAccess(user, RemoteAccessEventType.LOGIN_FAILED);
                    return;
                }
                roleList = collectRoles(authz);
            } finally {
                authz.clearSession();
            }
        }

        this.username = user;
        this.password = pass;
        this.authenticated = true;
        this.roles = parseRoles(roleList);
        this.availableServices = RemoteAccessServiceRegistry.availableServices(this.roles);
        this.selectedService = null;
        this.weatherHandler = null;
        this.airTransportHandler = null;

        send(out, RemoteAccessProtocol.OK + " " + user + " " + roleList);
        sendMenu(out);
        logRemoteAccess(user, RemoteAccessEventType.LOGIN_SUCCESS);
        RemoteAccessLog.log("User " + user + " logged in (roles: " + roleList + ")");
    }

    private void handleLogout(final BufferedWriter out) throws IOException {
        final String previousUser = username;
        if (authenticated) {
            logRemoteAccess(currentUsername(), RemoteAccessEventType.LOGOUT);
        }
        clearSession();
        send(out, RemoteAccessProtocol.OK + " Logged out.");
        if (previousUser != null) {
            RemoteAccessLog.log("User " + previousUser + " logged out");
        }
    }

    private boolean handleBack(final BufferedWriter out) throws IOException {
        if (selectedService != null) {
            RemoteAccessLog.log("User " + username + " left service " + selectedService.id());
        }
        selectedService = null;
        weatherHandler = null;
        airTransportHandler = null;
        send(out, RemoteAccessProtocol.OK + " Returned to service menu.");
        sendMenu(out);
        return true;
    }

    private void handleMenu(final BufferedWriter out) throws IOException {
        ensureAuthenticated();
        sendMenu(out);
    }

    private void handleSelect(final String line, final BufferedWriter out) throws IOException {
        ensureAuthenticated();
        final String[] parts = line.split("\\s+", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            send(out, RemoteAccessProtocol.ERR + " Usage: SELECT <service>");
            return;
        }
        final RemoteAccessService service = RemoteAccessService.fromId(parts[1]);
        if (service == null) {
            send(out, RemoteAccessProtocol.ERR + " Unknown service: " + parts[1].trim());
            return;
        }
        if (!availableServices.contains(service)) {
            send(out, RemoteAccessProtocol.ERR + " Service not available for your role: " + service.id());
            return;
        }

        selectedService = service;
        if (service == RemoteAccessService.WEATHER) {
            weatherHandler = new WeatherRemoteHandler(this);
            airTransportHandler = null;
        } else if (service == RemoteAccessService.ATCC) {
            airTransportHandler = new AirTransportCompanyRemoteHandler(this);
            weatherHandler = null;
        }
        send(out, RemoteAccessProtocol.OK + " Selected " + service.label()
                + " (" + service.userStory() + "). Type HELP for commands.");
        RemoteAccessLog.log("User " + username + " selected service " + service.id()
                + " (" + service.userStory() + ")");
    }

    private void sendMenu(final BufferedWriter out) throws IOException {
        final String menu = RemoteAccessServiceRegistry.formatMenu(availableServices);
        send(out, RemoteAccessProtocol.OK + " MENU " + menu);
    }

    private void sendHelp(final BufferedWriter out) throws IOException {
        if (selectedService == RemoteAccessService.WEATHER) {
            weatherHandler.sendHelp(out);
            return;
        }
        if (selectedService == RemoteAccessService.ATCC) {
            airTransportHandler.sendHelp(out);
            return;
        }
        send(out, RemoteAccessProtocol.OK + " Available commands:");
        if (!authenticated) {
            send(out, "  LOGIN <username> <password>");
            send(out, "  HELP | QUIT");
            return;
        }
        send(out, "  MENU");
        send(out, "  SELECT <service>");
        if (!availableServices.isEmpty()) {
            send(out, "  Available services:");
            for (final RemoteAccessService service : availableServices) {
                send(out, "    " + service.id() + " - " + service.label() + " (" + service.userStory() + ")");
            }
        } else {
            send(out, "  (No remote access services available for your role.)");
        }
        send(out, "  LOGOUT | QUIT | HELP");
    }

    private void clearSession() {
        username = null;
        password = null;
        authenticated = false;
        roles = Set.of();
        availableServices = List.of();
        selectedService = null;
        weatherHandler = null;
        airTransportHandler = null;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public boolean authenticated() {
        return authenticated;
    }

    @Override
    public String clientIp() {
        return socket.getInetAddress().getHostAddress();
    }

    @Override
    public int clientPort() {
        return socket.getPort();
    }

    @Override
    public void ensureAuthenticated() {
        if (!authenticated) {
            throw new SecurityException("Authentication required. Use LOGIN <username> <password>.");
        }
    }

    @Override
    public <T> T runAuthenticated(final Callable<T> action) {
        ensureAuthenticated();
        synchronized (AUTHZ_LOCK) {
            final AuthorizationService authz = AuthzRegistry.authorizationService();
            final AuthenticationController.LoginResult result =
                    new AuthenticationController().login(username, password);
            if (result != AuthenticationController.LoginResult.SUCCESS) {
                this.authenticated = false;
                this.selectedService = null;
                this.weatherHandler = null;
                this.airTransportHandler = null;
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

    private static String collectRoles(final AuthorizationService authz) {
        final List<String> roles = new ArrayList<>();
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

    private static Set<String> parseRoles(final String roleList) {
        if (roleList == null || roleList.isBlank() || "-".equals(roleList)) {
            return Set.of();
        }
        return Set.copyOf(List.of(roleList.split(",")));
    }

    private static String firstToken(final String line) {
        final int idx = line.indexOf(' ');
        return idx < 0 ? line : line.substring(0, idx);
    }

    private String currentUsername() {
        return username == null || username.isBlank() ? "-" : username;
    }

    private void logRemoteAccess(final String user, final RemoteAccessEventType eventType) {
        remoteAccessLogger.log(
                user,
                clientIp(),
                clientPort(),
                resolveLoggingService(),
                eventType);
    }

    private String resolveLoggingService() {
        if (selectedService != null) {
            return selectedService.userStory();
        }
        if (availableServices.size() == 1) {
            return availableServices.get(0).userStory();
        }
        return RemoteAccessService.WEATHER.userStory();
    }

    private static void sendSafe(final BufferedWriter out, final String message) {
        try {
            send(out, message);
        } catch (final IOException e) {
            RemoteAccessLog.log("Failed to send: " + e.getMessage());
        }
    }

    private static void send(final BufferedWriter out, final String message) throws IOException {
        writeLine(out, message);
        RemoteAccessLog.log(">> " + summarizeOutgoing(message));
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

    private void closeQuietly() {
        try {
            socket.close();
        } catch (final IOException ignored) {
            // already closing
        }
    }
}
