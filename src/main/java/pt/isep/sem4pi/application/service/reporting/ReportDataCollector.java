package pt.isep.sem4pi.application.service.reporting;

import java.time.YearMonth;

/**
 * Define como são procurados os dados de um relatório.
 * Permite usar outros tipos de recolha no futuro.
 */
public interface ReportDataCollector<T> {

    /**
     * Reúne os dados necessários para o período indicado.
     */
    T collect(YearMonth period);
}
