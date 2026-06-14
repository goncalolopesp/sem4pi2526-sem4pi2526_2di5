package pt.isep.sem4pi.application.service.reporting;

import java.time.YearMonth;

public interface ReportDataCollector<T> {

    T collect(YearMonth period);
}
