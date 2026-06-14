package pt.isep.sem4pi.domain.model.reporting;

import eapli.framework.validations.Preconditions;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Report {

    private final String type;
    private final String title;
    private final YearMonth period;
    private final LocalDateTime generatedAt;
    private final List<ReportSection> sections;

    protected Report(final String type,
                     final String title,
                     final YearMonth period,
                     final LocalDateTime generatedAt,
                     final List<ReportSection> sections) {
        Preconditions.nonEmpty(type, "Report type is required");
        Preconditions.nonEmpty(title, "Report title is required");
        Preconditions.nonNull(period, "Report period is required");
        Preconditions.nonNull(generatedAt, "Generation timestamp is required");
        Preconditions.nonNull(sections, "Report sections are required");
        this.type = type.trim();
        this.title = title.trim();
        this.period = period;
        this.generatedAt = generatedAt;
        this.sections = new ArrayList<>(sections);
    }

    public String type() {
        return type;
    }

    public String title() {
        return title;
    }

    public YearMonth period() {
        return period;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    public List<ReportSection> sections() {
        return Collections.unmodifiableList(sections);
    }
}
