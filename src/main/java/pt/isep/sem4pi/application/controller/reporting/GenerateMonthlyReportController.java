package pt.isep.sem4pi.application.controller.reporting;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.reporting.GenerateMonthlyReportService;
import pt.isep.sem4pi.application.viadto.GeneratedReportDTO;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.reporting.MonthlyReportServiceFactory;

import java.io.IOException;
import java.time.YearMonth;

@UseCaseController
public class GenerateMonthlyReportController {

    // DEFESA:
    // O controller valida autorização e converte input da UI; a geração pertence ao serviço de aplicação.
    private final AuthorizationService authz;
    private final GenerateMonthlyReportService service;

    public GenerateMonthlyReportController() {
        this.authz = AuthzRegistry.authorizationService();
        this.service = MonthlyReportServiceFactory.create();
    }

    public GenerateMonthlyReportController(final AuthorizationService authz,
                                           final GenerateMonthlyReportService service) {
        this.authz = authz;
        this.service = service;
    }

    public GeneratedReportDTO generate(final int year, final int month)
            throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR,
                SEM4PIRoles.ADMIN);
        return service.generate(YearMonth.of(year, month));
    }
}
