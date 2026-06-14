package pt.isep.sem4pi.domain.model.flight.DTO;

public class FlightTestExecutionResultDTO {

    private final boolean executed;
    private final boolean passed;
    private final String reportPath;
    private final String consoleOutput;

    public FlightTestExecutionResultDTO(final boolean executed,
                                        final boolean passed,
                                        final String reportPath,
                                        final String consoleOutput) {
        this.executed = executed;
        this.passed = passed;
        this.reportPath = reportPath;
        this.consoleOutput = consoleOutput;
    }

    public boolean executed() {
        return executed;
    }

    public boolean passed() {
        return passed;
    }

    public String reportPath() {
        return reportPath;
    }

    public String consoleOutput() {
        return consoleOutput;
    }
}
