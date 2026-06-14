package pt.isep.sem4pi.application.controller.reporting;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Test;
import pt.isep.sem4pi.application.service.reporting.GenerateMonthlyReportService;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.time.YearMonth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GenerateMonthlyReportControllerTest {

    @Test
    public void allowsFlightControlOperatorAndAdminRoles() throws Exception {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final GenerateMonthlyReportService service =
                mock(GenerateMonthlyReportService.class);
        final GenerateMonthlyReportController controller =
                new GenerateMonthlyReportController(authz, service);

        controller.generate(2026, 6);

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR,
                SEM4PIRoles.ADMIN);
        verify(service).generate(YearMonth.of(2026, 6));
    }
}
