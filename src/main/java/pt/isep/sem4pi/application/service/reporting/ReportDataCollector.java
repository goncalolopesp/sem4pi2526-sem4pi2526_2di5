package pt.isep.sem4pi.application.service.reporting;

import java.time.YearMonth;

public interface ReportDataCollector<T> {

    // DEFESA:
    // Protected Variations: o caso de uso depende do contrato de recolha, não dos repositórios concretos.
    T collect(YearMonth period);
}
