package pt.isep.sem4pi.application.viadto;

import java.time.LocalDateTime;
import java.time.YearMonth;

public record GeneratedReportDTO(
        String reportId,
        String title,
        YearMonth period,
        LocalDateTime generatedAt,
        String format,
        String fileLocation) {
}
