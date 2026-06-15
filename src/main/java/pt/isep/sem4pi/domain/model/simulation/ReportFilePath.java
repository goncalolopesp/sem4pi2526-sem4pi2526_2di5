package pt.isep.sem4pi.domain.model.simulation;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.nio.file.Path;
import java.util.Objects;

@Embeddable
public class ReportFilePath {

    // DEFESA:
    // Este Value Object normaliza o caminho e evita tratar a localização do relatório como uma String sem significado.
    @Column(name = "file_location", nullable = false, length = 1000)
    private String value;

    protected ReportFilePath() {
        // for ORM
    }

    public ReportFilePath(final String value) {
        Preconditions.nonEmpty(value, "Report file path is required");
        this.value = Path.of(value).normalize().toString();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ReportFilePath
                && value.equals(((ReportFilePath) other).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
