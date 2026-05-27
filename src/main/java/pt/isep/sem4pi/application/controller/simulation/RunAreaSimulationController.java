package pt.isep.sem4pi.application.controller.simulation;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.simulation.*;
import pt.isep.sem4pi.application.viadto.SimulationDTORepresentationBuilder;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationDTO;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.repositories.SimulationRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.simulation.CFlightSimulatorRunner;
import pt.isep.sem4pi.domain.model.simulation.SimulationReport;
import pt.isep.sem4pi.domain.repositories.SimulationReportRepository;

import java.time.LocalDateTime;
import java.util.List;

@UseCaseController
public class RunAreaSimulationController {

    private final AuthorizationService authz;
    private final SimulationRepository simulationRepository;
    private final RunAreaSimulationService runAreaSimulationService;
    private final SimulationRunner simulationRunner;
    private final SimulationReportRepository simulationReportRepository;
    private final SimulationReportParser simulationReportParser;
    private final SimulationJobService simulationJobService;

    public RunAreaSimulationController() {
        this.authz = AuthzRegistry.authorizationService();
        this.simulationRepository = PersistenceContext.repositories().simulations();
        this.runAreaSimulationService = new RunAreaSimulationService(authz);
        this.simulationRunner = new CFlightSimulatorRunner();
        this.simulationReportRepository = PersistenceContext.repositories().simulationReports();
        this.simulationReportParser = new SimulationReportParser();
        this.simulationJobService =
                new SimulationJobService(
                        this.simulationRepository,
                        this.simulationReportRepository,
                        this.simulationRunner,
                        this.simulationReportParser);
    }

    public RunAreaSimulationController(final AuthorizationService authz, final SimulationRepository simulationRepository, final RunAreaSimulationService runAreaSimulationService, final SimulationRunner simulationRunner, final SimulationReportRepository simulationReportRepository, final SimulationReportParser simulationReportParser, final SimulationJobService simulationJobService) {
        this.authz = authz;
        this.simulationRepository = simulationRepository;
        this.runAreaSimulationService = runAreaSimulationService;
        this.simulationRunner = simulationRunner;
        this.simulationReportRepository = simulationReportRepository;
        this.simulationReportParser = simulationReportParser;
        this.simulationJobService = simulationJobService;
    }

    public SimulationDTO createSimulation(final String airControlAreaId, final LocalDateTime startTime, final LocalDateTime endTime, final Integer timeStep, final Double safetyThreshold, final List<String> flightPlanIds) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.FLIGHT_CONTROL_OPERATOR, SEM4PIRoles.ADMIN);

        final Simulation simulation = runAreaSimulationService.createSimulation(airControlAreaId, startTime, endTime, timeStep, safetyThreshold, flightPlanIds);

        final Simulation savedSimulation = simulationRepository.save(simulation);

        final SimulationDTORepresentationBuilder builder = new SimulationDTORepresentationBuilder();

        return savedSimulation.buildRepresentation(builder);
    }

    public SimulationExecutionResult runSimulation(final String simulationId) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.FLIGHT_CONTROL_OPERATOR, SEM4PIRoles.ADMIN);

        final Simulation simulation = simulationRepository.ofIdentity(simulationId).orElseThrow(() -> new IllegalArgumentException("Simulation not found."));

        simulation.markAsRunning();
        simulationRepository.save(simulation);

        final SimulationExecutionResult result = simulationRunner.run(simulation);

        if (result.success()) {

            try {

                final SimulationReport report =
                        simulationReportParser.parse(
                                simulation.simulationId(),
                                result.reportPath());

                simulationReportRepository.save(report);

                if ("FAIL".equalsIgnoreCase(report.finalValidationResult())) {
                    simulation.markAsFailed();
                } else {
                    simulation.markAsFinished();
                }

            } catch (Exception e) {
                simulation.markAsFailed();
            }

        } else {
            simulation.markAsFailed();
        }

        simulationRepository.save(simulation);

        return result;
    }

    public void submitSimulation(final String simulationId) {

        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR,
                SEM4PIRoles.ADMIN);

        simulationJobService.submit(simulationId);
    }
}