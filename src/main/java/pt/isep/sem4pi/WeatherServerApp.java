package pt.isep.sem4pi;

/**
 * @deprecated Use {@link MainServer} instead. Kept for backward compatibility with existing scripts.
 */
@Deprecated
public final class WeatherServerApp {

    private WeatherServerApp() {
        // launcher
    }

    public static void main(final String[] args) throws Exception {
        MainServer.main(args);
    }
}
