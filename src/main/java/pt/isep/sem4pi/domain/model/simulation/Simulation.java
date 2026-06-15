// Existe como Aggregate Root da US100: concentra as regras e protege o estado da simulação.
package pt.isep.sem4pi.domain.model.simulation;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.representations.RepresentationBuilder;
import eapli.framework.representations.Representationable;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

/**
 * Entidade principal da simulação e Aggregate Root.
 * Guarda os dados e controla as mudanças de estado.
 */
@Entity
public class Simulation implements AggregateRoot<String>, Representationable {

    @Id
    @Column(nullable = false, unique = true)
    private String simulationId;

    @Column(nullable = false)
    private String airControlAreaId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer timeStep;

    @Column(nullable = false)
    private Double safetyThreshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SimulationStatus status;

    @Column(length = 4000)
    private String failureReason;

    @Column(nullable = false)
    private Double minLat;

    @Column(nullable = false)
    private Double maxLat;

    @Column(nullable = false)
    private Double minLon;

    @Column(nullable = false)
    private Double maxLon;

    @Column(nullable = false)
    private Integer startSecond;

    @Column(nullable = false)
    private Integer endSecond;

    @Column(nullable = false)
    private String weather;

    @Column(nullable = false)
    private Double safetyMinSepNm;

    @Column(nullable = false)
    private Double safetyMinAltM;

    @Column(nullable = false)
    private Integer violationThreshold;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "simulation_flight_inputs",
            joinColumns = @JoinColumn(name = "simulation_id")
    )
    private Set<SimulatedFlightInput> simulatedFlightInputs = new HashSet<>();

    @Version
    private Long version;

    protected Simulation() {
        // for ORM
    }

    /**
     * Cria uma simulação válida no estado inicial CREATED.
     * As validações impedem que seja criada com dados inválidos.
     */
    public Simulation(final String simulationId,
                      final String airControlAreaId,
                      final LocalDateTime startTime,
                      final LocalDateTime endTime,
                      final Integer timeStep,
                      final Double safetyThreshold,
                      final Double minLat,
                      final Double maxLat,
                      final Double minLon,
                      final Double maxLon,
                      final Integer startSecond,
                      final Integer endSecond,
                      final String weather,
                      final Double safetyMinSepNm,
                      final Double safetyMinAltM,
                      final Integer violationThreshold) {

        Preconditions.nonEmpty(simulationId, "Simulation id is required");
        Preconditions.nonEmpty(airControlAreaId, "Air control area id is required");
        Preconditions.nonNull(startTime, "Start time is required");
        Preconditions.nonNull(endTime, "End time is required");
        Preconditions.nonNull(timeStep, "Time step is required");
        Preconditions.nonNull(safetyThreshold, "Safety threshold is required");

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (timeStep <= 0) {
            throw new IllegalArgumentException("Time step must be positive");
        }

        if (safetyThreshold <= 0) {
            throw new IllegalArgumentException("Safety threshold must be positive");
        }

        this.simulationId = simulationId.trim();
        this.airControlAreaId = airControlAreaId.trim();
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeStep = timeStep;
        this.safetyThreshold = safetyThreshold;
        this.status = SimulationStatus.CREATED;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.startSecond = startSecond;
        this.endSecond = endSecond;
        this.timeStep = timeStep;
        this.weather = weather.trim().toLowerCase();
        this.safetyMinSepNm = safetyMinSepNm;
        this.safetyMinAltM = safetyMinAltM;
        this.violationThreshold = violationThreshold;
    }

    /**
     * Acrescenta um plano de voo aos dados de entrada da simulação.
     */
    public void addFlightPlan(final String flightPlanId) {
        Preconditions.nonEmpty(flightPlanId, "Flight plan id is required");

        this.simulatedFlightInputs.add(new SimulatedFlightInput(flightPlanId));
    }

    /**
     * Inicia a simulação, apenas quando esta ainda está em CREATED.
     */
    public void markAsRunning() {
        if (status != SimulationStatus.CREATED) {
            throw new IllegalStateException("Only created simulations can be started");
        }

        this.status = SimulationStatus.RUNNING;
    }

    /**
     * Conclui a simulação, apenas depois de esta ter sido iniciada.
     */
    public void markAsFinished() {
        if (status != SimulationStatus.RUNNING) {
            throw new IllegalStateException("Only running simulations can be finished");
        }

        this.status = SimulationStatus.FINISHED;
    }

    /**
     * Regista uma falha genérica na simulação.
     */
    public void markAsFailed() {
        markAsFailed("Simulation failed");
    }

    public long durationInSeconds() {
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    public String simulationId() {
        return simulationId;
    }

    public String airControlAreaId() {
        return airControlAreaId;
    }

    public LocalDateTime startTime() {
        return startTime;
    }

    public LocalDateTime endTime() {
        return endTime;
    }

    public Integer timeStep() {
        return timeStep;
    }

    public Double safetyThreshold() {
        return safetyThreshold;
    }

    public SimulationStatus status() {
        return status;
    }

    public Set<SimulatedFlightInput> simulatedFlightInputs() {
        return Set.copyOf(simulatedFlightInputs);
    }

    public Double minLat() {
        return minLat;
    }

    public Double maxLat() {
        return maxLat;
    }

    public Double minLon() {
        return minLon;
    }

    public Double maxLon() {
        return maxLon;
    }

    public Integer startSecond() {
        return startSecond;
    }

    public Integer endSecond() {
        return endSecond;
    }

    public String weather() {
        return weather;
    }

    public Double safetyMinSepNm() {
        return safetyMinSepNm;
    }

    public Double safetyMinAltM() {
        return safetyMinAltM;
    }

    public Integer violationThreshold() {
        return violationThreshold;
    }

    public Long version() {
        return version;
    }

    @Override
    public String identity() {
        return simulationId;
    }

    @Override
    public boolean hasIdentity(final String id) {
        return simulationId.equals(id);
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Simulation)) {
            return false;
        }

        final Simulation that = (Simulation) other;
        return this.simulationId.equals(that.simulationId);
    }

    @Override
    public boolean equals(final Object other) {
        return sameAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulationId);
    }

    @Override
    public <R> R buildRepresentation(
            final RepresentationBuilder<R> builder) {

        builder.startObject("Simulation")
                .withProperty("simulationId", simulationId)
                .withProperty("airControlAreaId", airControlAreaId)
                .withProperty("startTime", startTime.toString())
                .withProperty("endTime", endTime.toString())
                .withProperty("timeStep", timeStep)
                .withProperty("safetyThreshold", safetyThreshold)
                .withProperty("status", status.name())
                .withProperty("version", version);

        builder.startCollection("flightPlanIds");

        for (SimulatedFlightInput input : simulatedFlightInputs) {
            builder.withElement(input.flightPlanId());
        }

        builder.endCollection();

        builder.endObject();

        return builder.build();
    }

    /**
     * Regista a falha e guarda uma razão adequada à persistência.
     */
    public void markAsFailed(final String reason) {
        if (status == SimulationStatus.FINISHED) {
            throw new IllegalStateException("Finished simulations cannot be marked as failed");
        }

        this.status = SimulationStatus.FAILED;

        if (reason == null) {
            this.failureReason = "Simulation failed";
        } else if (reason.length() > 4000) {
            this.failureReason = reason.substring(0, 4000);
        } else {
            this.failureReason = reason;
        }
    }

    public String failureReason() {
        return failureReason;
    }
}
