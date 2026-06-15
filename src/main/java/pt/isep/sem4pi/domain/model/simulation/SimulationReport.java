// Existe como Aggregate Root da US111: calcula e protege o resultado final do relatório.
package pt.isep.sem4pi.domain.model.simulation;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade principal do resumo e Aggregate Root.
 * Guarda os resultados e decide se a simulação passou ou falhou.
 */
@Entity
public class SimulationReport implements AggregateRoot<String> {

    @Id
    @Column(nullable = false, unique = true)
    private String reportId;

    @Column(nullable = false, unique = true)
    private String simulationId;

    @Column(nullable = false)
    private Integer totalFlights;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SimulationValidationResult finalValidationResult;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Embedded
    private ReportFilePath fileLocation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "simulation_report_flights",
            joinColumns = @JoinColumn(name = "report_id"))
    private List<FlightExecutionSummary> flightSummaries = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "simulation_report_violations",
            joinColumns = @JoinColumn(name = "report_id"))
    private List<SafetyViolationSummary> safetyViolations = new ArrayList<>();

    @Version
    private Long version;

    protected SimulationReport() {
        // for ORM
    }

    /**
     * Cria o relatório e calcula a validação final a partir dos seus dados.
     */
    public SimulationReport(final String reportId,
                            final String simulationId,
                            final LocalDateTime generatedAt,
                            final ReportFilePath fileLocation,
                            final List<FlightExecutionSummary> flightSummaries,
                            final List<SafetyViolationSummary> safetyViolations,
                            final SimulationValidationResult scompResult) {
        Preconditions.nonEmpty(reportId, "Report id is required");
        Preconditions.nonEmpty(simulationId, "Simulation id is required");
        Preconditions.nonNull(generatedAt, "Generated at is required");
        Preconditions.nonNull(fileLocation, "File location is required");
        Preconditions.nonNull(flightSummaries, "Flight summaries are required");
        Preconditions.nonNull(safetyViolations, "Safety violations are required");
        Preconditions.nonNull(scompResult, "Validation result is required");

        this.reportId = reportId.trim();
        this.simulationId = simulationId.trim();
        this.generatedAt = generatedAt;
        this.fileLocation = fileLocation;
        this.flightSummaries = new ArrayList<>(flightSummaries);
        this.safetyViolations = new ArrayList<>(safetyViolations);
        this.totalFlights = flightSummaries.size();
        this.finalValidationResult = calculateValidationResult(scompResult);
    }

    /**
     * Aplica a regra de negócio que decide PASS ou FAIL.
     */
    private SimulationValidationResult calculateValidationResult(
            final SimulationValidationResult scompResult) {
        // Basta uma falha, violação ou voo incompleto para o resultado ser FAIL.
        if (scompResult == SimulationValidationResult.FAIL) {
            return SimulationValidationResult.FAIL;
        }

        if (!safetyViolations.isEmpty()) {
            return SimulationValidationResult.FAIL;
        }

        final boolean hasUnsuccessfulFlight = flightSummaries.stream()
                .anyMatch(summary -> !summary.completedSuccessfully());
        return hasUnsuccessfulFlight
                ? SimulationValidationResult.FAIL
                : SimulationValidationResult.PASS;
    }

    public String reportId() {
        return reportId;
    }

    public String simulationId() {
        return simulationId;
    }

    public Integer totalFlights() {
        return totalFlights;
    }

    public SimulationValidationResult validationResult() {
        return finalValidationResult;
    }

    public String finalValidationResult() {
        return finalValidationResult.name();
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    public String fileLocation() {
        return fileLocation.value();
    }

    public List<FlightExecutionSummary> flightSummaries() {
        return List.copyOf(flightSummaries);
    }

    public List<SafetyViolationSummary> safetyViolations() {
        return List.copyOf(safetyViolations);
    }

    public Long version() {
        return version;
    }

    @Override
    public String identity() {
        return reportId;
    }

    @Override
    public boolean hasIdentity(final String id) {
        return reportId.equals(id);
    }

    @Override
    public boolean sameAs(final Object other) {
        return other instanceof SimulationReport
                && reportId.equals(((SimulationReport) other).reportId);
    }

    @Override
    public boolean equals(final Object other) {
        return sameAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
}
