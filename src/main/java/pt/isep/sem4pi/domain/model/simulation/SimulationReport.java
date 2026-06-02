package pt.isep.sem4pi.domain.model.simulation;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class SimulationReport implements AggregateRoot<String> {

    @Id
    @Column(nullable = false, unique = true)
    private String reportId;

    @Column(nullable = false)
    private String simulationId;

    @Column(nullable = false)
    private Integer totalFlights;

    @Column(nullable = false)
    private String finalValidationResult;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private String fileLocation;

    @Version
    private Long version;

    protected SimulationReport() {
        // for ORM
    }

    public SimulationReport(final String reportId,
                            final String simulationId,
                            final Integer totalFlights,
                            final String finalValidationResult,
                            final LocalDateTime generatedAt,
                            final String fileLocation) {

        Preconditions.nonEmpty(reportId, "Report id is required");
        Preconditions.nonEmpty(simulationId, "Simulation id is required");
        Preconditions.nonNull(totalFlights, "Total flights is required");
        Preconditions.nonEmpty(finalValidationResult, "Final validation result is required");
        Preconditions.nonNull(generatedAt, "Generated at is required");
        Preconditions.nonEmpty(fileLocation, "File location is required");

        this.reportId = reportId.trim();
        this.simulationId = simulationId.trim();
        this.totalFlights = totalFlights;
        this.finalValidationResult = finalValidationResult.trim();
        this.generatedAt = generatedAt;
        this.fileLocation = fileLocation.trim();
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

    public String finalValidationResult() {
        return finalValidationResult;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    public String fileLocation() {
        return fileLocation;
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
        if (!(other instanceof SimulationReport)) {
            return false;
        }

        final SimulationReport that = (SimulationReport) other;
        return this.reportId.equals(that.reportId);
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