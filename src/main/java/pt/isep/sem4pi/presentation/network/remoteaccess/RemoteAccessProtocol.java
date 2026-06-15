package pt.isep.sem4pi.presentation.network.remoteaccess;

public final class RemoteAccessProtocol {

    private RemoteAccessProtocol() {
        // utility
    }

    public static final int DEFAULT_PORT = 8044;

    public static final String CMD_HELLO = "HELLO";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_QUIT = "QUIT";
    public static final String CMD_HELP = "HELP";
    public static final String CMD_MENU = "MENU";
    public static final String CMD_SELECT = "SELECT";
    public static final String CMD_BACK = "BACK";

    public static final String OK = "OK";
    public static final String ERR = "ERR";

    public static final String FIELD_SEP = ";";

    public static int resolvePort() {
        final String prop = System.getProperty("remote.access.server.port");
        if (prop != null && !prop.isBlank()) {
            return Integer.parseInt(prop.trim());
        }
        final String env = System.getenv("REMOTE_ACCESS_SERVER_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env.trim());
        }
        final String weatherProp = System.getProperty("weather.server.port");
        if (weatherProp != null && !weatherProp.isBlank()) {
            return Integer.parseInt(weatherProp.trim());
        }
        final String weatherEnv = System.getenv("WEATHER_SERVER_PORT");
        if (weatherEnv != null && !weatherEnv.isBlank()) {
            return Integer.parseInt(weatherEnv.trim());
        }
        return DEFAULT_PORT;
    }
}
