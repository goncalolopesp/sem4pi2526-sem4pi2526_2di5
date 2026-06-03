package pt.isep.sem4pi.application.service.simulation;

public class SimulationExecutionResult {

    private final boolean success;
    private final String reportPath;
    private final String consoleOutput;

    public SimulationExecutionResult(final boolean success,
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