package pt.isep.sem4pi.infrastructure.reporting;

import pt.isep.sem4pi.application.service.reporting.GenerateMonthlyReportService;
import pt.isep.sem4pi.application.service.reporting.MonthlyStatisticsDataCollector;
import pt.isep.sem4pi.application.service.reporting.MonthlyStatisticsReportBuilder;
import pt.isep.sem4pi.application.service.reporting.TextReportRenderer;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Cria o serviço da US112 com todas as classes de que precisa.
 * Assim, o controller não tem de montar cada dependência.
 */
public final class MonthlyReportServiceFactory {

    private MonthlyReportServiceFactory() {
    }

    /**
     * Liga os repositórios às classes que criam o relatório.
     */
    public static GenerateMonthlyReportService create() {
        final RepositoryFactory repositories = PersistenceContext.repositories();
        return new GenerateMonthlyReportService(
                new MonthlyStatisticsDataCollector(
                        repositories.simulations(),
                        repositories.simulationReports(),
                        repositories.flights(),
                        new SimulationReportFileViolationReader()),
                new MonthlyStatisticsReportBuilder(),
                new TextReportRenderer(),
                repositories,
                Path.of("reports"),
                Clock.systemDefaultZone());
    }
}
