package pt.isep.sem4pi.application.controller.simulation;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.viadto.SimulationSummaryReportDTOMapper;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationStatusDTO;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationSummaryReportDTO;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

@UseCaseController
public class CheckSimulationStatusController {

    private final AuthorizationService authz;
    private final SimulationRepository simulations;
    private final SimulationReportRepository reports;
    private final SimulationSummaryReportDTOMapper reportMapper;

    public CheckSimulationStatusController() {
        this(
                AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().simulations(),
                PersistenceContext.repositories().simulationReports(),
                new SimulationSummaryReportDTOMapper());
    }

    public CheckSimulationStatusController(
            final AuthorizationService authz,
            final SimulationRepository simulations,
            final SimulationReportRepository reports,
            final SimulationSummaryReportDTOMapper reportMapper) {
        this.authz = authz;
        this.simulations = simulations;
        this.reports = reports;
        this.reportMapper = reportMapper;
    }

    public SimulationStatusDTO statusOf(final String simulationId) {
        ensureAuthorized();

        final Simulation simulation = simulations.ofIdentity(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found"));
        final SimulationReport report =
                reports.findBySimulationId(simulationId).orElse(null);

        return new SimulationStatusDTO(
                simulation.simulationId(),
                simulation.status().name(),
                simulation.failureReason(),
                report == null ? null : reportMapper.toDTO(report));
    }

    public SimulationSummaryReportDTO reportOf(final String simulationId) {
        ensureAuthorized();

        final SimulationReport report = reports.findBySimulationId(simulationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Simulation report not found. The simulation may still be running."));
        return reportMapper.toDTO(report);
    }

    private void ensureAuthorized() {
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR,
                SEM4PIRoles.ADMIN);
    }
}
