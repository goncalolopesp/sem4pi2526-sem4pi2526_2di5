package pt.isep.sem4pi.application.service.simulation;

/**
 * Resultado devolvido pelo simulador.
 * Indica se correu bem e onde ficou o relatório.
 */
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

    /**
     * Indica se a execução terminou com sucesso.
     */
    public boolean success() {
        return success;
    }

    /**
     * Devolve o caminho do relatório criado pelo simulador.
     */
    public String reportPath() {
        return reportPath;
    }

    /**
     * Devolve a mensagem produzida durante a execução.
     */
    public String consoleOutput() {
        return consoleOutput;
    }
}
