package pt.isep.sem4pi.application.service.flightplan;

public class FlightTestExecutionResult {

    private final boolean success;
    private final String reportPath;
    private final String consoleOutput;

    public FlightTestExecutionResult(final boolean success,
                                     final String reportPath,
                                     final String consoleOutput) {
        this.success = success;
        this.reportPath = reportPath;
        this.consoleOutput = consoleOutput;
    }

    public boolean success() {
        return success;
    }

    public String reportPath() {
        return reportPath;
    }

    public String consoleOutput() {
        return consoleOutput;
    }
}
