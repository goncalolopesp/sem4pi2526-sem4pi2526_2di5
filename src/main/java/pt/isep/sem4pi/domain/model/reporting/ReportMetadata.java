package pt.isep.sem4pi.domain.model.reporting;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

@Entity
public class ReportMetadata implements AggregateRoot<String> {

    // DEFESA:
    // Este Aggregate Root representa o registo persistente de um relatório gerado, não o conteúdo do ficheiro.
    @Id
    @Column(nullable = false, unique = true)
    private String reportId;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer periodYear;

    @Column(nullable = false)
    private Integer periodMonth;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false, length = 1000)
    private String fileLocation;

    @Version
    // DEFESA:
    // A versão permite detetar escritas concorrentes sem bloquear antecipadamente o registo.
    private Long version;

    protected ReportMetadata() {
        // for ORM
    }

    public ReportMetadata(final String reportId,
                          final String reportType,
                          final String title,
                          final YearMonth period,
                          final LocalDateTime generatedAt,
                          final String format,
                          final String fileLocation) {
        Preconditions.nonEmpty(reportId, "Report id is required");
        Preconditions.nonEmpty(reportType, "Report type is required");
        Preconditions.nonEmpty(title, "Report title is required");
        Preconditions.nonNull(period, "Report period is required");
        Preconditions.nonNull(generatedAt, "Generation timestamp is required");
        Preconditions.nonEmpty(format, "Report format is required");
        Preconditions.nonEmpty(fileLocation, "File location is required");
        this.reportId = reportId.trim();
        this.reportType = reportType.trim();
        this.title = title.trim();
        this.periodYear = period.getYear();
        this.periodMonth = period.getMonthValue();
        this.generatedAt = generatedAt;
        this.format = format.trim();
        this.fileLocation = fileLocation.trim();
    }

    public String reportId() {
        return reportId;
    }

    public String reportType() {
        return reportType;
    }

    public String title() {
        return title;
    }

    public YearMonth period() {
        return YearMonth.of(periodYear, periodMonth);
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    public String format() {
        return format;
    }

    public String fileLocation() {
        return fileLocation;
    }

    @Override
    public String identity() {
        return reportId;
    }

    @Override
    public boolean sameAs(final Object other) {
        return other instanceof ReportMetadata that
                && reportId.equals(that.reportId);
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
