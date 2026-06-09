package pt.isep.sem4pi.presentation.network.weather;

public final class WeatherProtocol {

    private WeatherProtocol() {
        // utility
    }

    public static final int DEFAULT_PORT = 8044;

    public static final String CMD_HELLO = "HELLO";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_IMPORT = "IMPORT";
    public static final String CMD_CONSULT = "CONSULT";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_QUIT = "QUIT";
    public static final String CMD_HELP = "HELP";

    public static final String OK = "OK";
    public static final String ERR = "ERR";

    public static final String FIELD_SEP = ";";

    public static final String AUTODETECT = "-";

    /** Sent periodically while a bulk import is being processed (keeps the TCP session alive). */
    public static final String IMPORT_IN_PROGRESS = "IMPORT_IN_PROGRESS";

    public static int resolvePort() {
        final String prop = System.getProperty("weather.server.port");
        if (prop != null && !prop.isBlank()) {
            return Integer.parseInt(prop.trim());
        }
        final String env = System.getenv("WEATHER_SERVER_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env.trim());
        }
        return DEFAULT_PORT;
    }
}
