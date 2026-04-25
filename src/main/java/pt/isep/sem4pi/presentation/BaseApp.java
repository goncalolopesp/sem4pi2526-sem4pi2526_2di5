package pt.isep.sem4pi.presentation;

/**
 * Minimal BaseApp similar to the example's BaseApp to run console applications.
 */
public abstract class BaseApp {

    protected static final String SEPARATOR_HR = "=====================================";

    public void run(final String[] args) {
        printHeader();
        try {
            doSetupEventHandlers();
            doMain(args);
            printFooter();
        } catch (final Exception e) {
            System.out.println("Something unexpected has happened and the application will terminate. Please check the logs.\n");
            e.printStackTrace();
        }
        // exit application
        System.exit(0);
    }

    protected void printFooter() {
        System.out.println("\n");
        System.out.println(SEPARATOR_HR);
        System.out.println(appGoodbye());
        System.out.println(SEPARATOR_HR);
    }

    protected void printHeader() {
        System.out.println(SEPARATOR_HR);
        System.out.println(appTitle());
        System.out.println(SEPARATOR_HR);
    }

    protected void doSetupEventHandlers() {
        // default
    }

    protected abstract void doMain(final String[] args);

    protected abstract String appTitle();

    protected abstract String appGoodbye();
}
