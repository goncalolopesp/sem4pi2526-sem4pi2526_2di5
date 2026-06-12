package pt.isep.sem4pi.application.controller.reporting;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.reporting.GenerateMonthlyReportService;
import pt.isep.sem4pi.application.service.reporting.GeneratedReportDTO;
import pt.isep.sem4pi.application.service.reporting.MonthlyStatisticsDataCollector;
import pt.isep.sem4pi.application.service.reporting.MonthlyStatisticsReportBuilder;
import pt.isep.sem4pi.application.service.reporting.TextReportRenderer;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;

@UseCaseController
public class GenerateMonthlyReportController {

    private final AuthorizationService authz;
    private final GenerateMonthlyReportService service;

    public GenerateMonthlyReportController() {
        final RepositoryFactory repositories = PersistenceContext.repositories();
        this.authz = AuthzRegistry.authorizationService();
        this.service = new GenerateMonthlyReportService(
                new MonthlyStatisticsDataCollector(
                        repositories.simulations(),
                        repositories.simulationReports(),
                        repositories.flights()),
                new MonthlyStatisticsReportBuilder(),
                new TextReportRenderer(),
                repositories,
                Path.of("reports"),
                Clock.systemDefaultZone());
    }

    public GenerateMonthlyReportController(final AuthorizationService authz,
                                           final GenerateMonthlyReportService service) {
        this.authz = authz;
        this.service = service;
    }

    public GeneratedReportDTO generate(final int year, final int month)
            throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.BACKOFFICE_OPERATOR,
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR,
                SEM4PIRoles.ADMIN);
        return service.generate(YearMonth.of(year, month));
    }
}
