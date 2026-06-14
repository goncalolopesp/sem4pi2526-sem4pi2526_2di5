package pt.isep.sem4pi.infrastructure.reporting;

import pt.isep.sem4pi.application.service.reporting.GenerateMonthlyReportService;
import pt.isep.sem4pi.application.service.reporting.MonthlyStatisticsDataCollector;
import pt.isep.sem4pi.application.service.reporting.MonthlyStatisticsReportBuilder;
import pt.isep.sem4pi.application.service.reporting.TextReportRenderer;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.nio.file.Path;
import java.time.Clock;

public final class MonthlyReportServiceFactory {

    private MonthlyReportServiceFactory() {
    }

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
