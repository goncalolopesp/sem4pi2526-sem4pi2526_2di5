package pt.isep.sem4pi.domain.model.simulation;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class SafetyViolationSummary {

    // DEFESA:
    // Este Value Object preserva, como um valor imutável do relatório, o instante, tipo, posição e detalhe.
    @Column(name = "violation_timestamp", nullable = false)
    private Integer timestamp;

    @Column(name = "violation_type", nullable = false)
    private String type;

    @Column(name = "violation_position", nullable = false, length = 1000)
    private String position;

    @Column(name = "violation_details", nullable = false, length = 2000)
    private String details;

    protected SafetyViolationSummary() {
        // for ORM
    }

    public SafetyViolationSummary(final Integer timestamp,
                                  final String type,
                                  final String position,
                                  final String details) {
        Preconditions.nonNull(timestamp, "Violation timestamp is required");
        Preconditions.nonEmpty(type, "Violation type is required");
        Preconditions.nonEmpty(position, "Violation position is required");
        Preconditions.nonEmpty(details, "Violation details are required");
        if (timestamp < 0) {
            throw new IllegalArgumentException("Violation timestamp cannot be negative");
        }
        this.timestamp = timestamp;
        this.type = type.trim();
        this.position = position.trim();
        this.details = details.trim();
    }

    public Integer timestamp() {
        return timestamp;
    }

    public String type() {
        return type;
    }

    public String position() {
        return position;
    }

    public String details() {
        return details;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SafetyViolationSummary)) {
            return false;
        }
        final SafetyViolationSummary that = (SafetyViolationSummary) other;
        return timestamp.equals(that.timestamp)
                && type.equals(that.type)
                && position.equals(that.position)
                && details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, type, position, details);
    }
}
