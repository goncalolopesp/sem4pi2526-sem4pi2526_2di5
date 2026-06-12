package pt.isep.sem4pi.application.service.reporting;

import pt.isep.sem4pi.domain.model.reporting.Report;
import pt.isep.sem4pi.domain.model.reporting.ReportSection;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public final class MonthlyStatisticsReport extends Report {

    public static final String TYPE = "MONTHLY_STATISTICS";

    public MonthlyStatisticsReport(final YearMonth period,
                                   final LocalDateTime generatedAt,
                                   final List<ReportSection> sections) {
        super(TYPE, "AIControl Monthly Statistics Report", period, generatedAt, sections);
    }
}
