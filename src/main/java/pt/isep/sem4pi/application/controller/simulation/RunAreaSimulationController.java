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

import java.util.List;

@UseCaseController
public class RunAreaSimulationController {

    // DEFESA:
    // O controller coordena autorização e caso de uso, mas delega regras, persistência e execução externa.
    private final AuthorizationService authz;
    private final SimulationRepository simulationRepository;
    private final RunAreaSimulationService runAreaSimulationService;
    private final SimulationRunner simulationRunner;
    private final GenerateSimulationSummaryReportService reportService;
    private final SimulationJobService simulationJobService;

    public RunAreaSimulationController() {
        // DEFESA:
        // Esta composição liga as abstrações da aplicação às implementações reais usadas em produção.
        this.authz = AuthzRegistry.authorizationService();
        this.simulationRepository = PersistenceContext.repositories().simulations();
        this.runAreaSimulationService = new RunAreaSimulationService(authz);
        this.simulationRunner = new CFlightSimulatorRunner();
        this.reportService = new GenerateSimulationSummaryReportService(
                PersistenceContext.repositories(),
                new SimulationReportParser(),
                new SimulationSummaryReportExporter());
        this.simulationJobService =
                new SimulationJobService(
                        this.simulationRepository,
                        this.simulationRunner,
                        this.reportService);
    }

    public RunAreaSimulationController(
            final AuthorizationService authz,
            final SimulationRepository simulationRepository,
            final RunAreaSimulationService runAreaSimulationService,
            final SimulationRunner simulationRunner,
            final GenerateSimulationSummaryReportService reportService,
            final SimulationJobService simulationJobService) {
        this.authz = authz;
        this.simulationRepository = simulationRepository;
        this.runAreaSimulationService = runAreaSimulationService;
        this.simulationRunner = simulationRunner;
        this.reportService = reportService;
        this.simulationJobService = simulationJobService;
    }

    public SimulationDTO createSimulation(final String airControlAreaId,
                                          final Double minLat,
                                          final Double maxLat,
                                          final Double minLon,
                                          final Double maxLon,
                                          final Integer startSecond,
                                          final Integer endSecond,
                                          final Integer timeStep,
                                          final String weather,
                                          final Double safetyMinSepNm,
                                          final Double safetyMinAltM,
                                          final Integer violationThreshold,
                                          final List<String> flightPlanIds) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.FLIGHT_CONTROL_OPERATOR, SEM4PIRoles.ADMIN);

        final Simulation simulation =
                runAreaSimulationService.createSimulation(
                        airControlAreaId,
                        minLat,
                        maxLat,
                        minLon,
                        maxLon,
                        startSecond,
                        endSecond,
                        timeStep,
                        weather,
                        safetyMinSepNm,
                        safetyMinAltM,
                        violationThreshold,
                        flightPlanIds);

        // DEFESA:
        // O Aggregate Root é persistido pelo Repository; o controller não conhece JPA nem SQL.
        final Simulation savedSimulation = simulationRepository.save(simulation);

        final SimulationDTORepresentationBuilder builder = new SimulationDTORepresentationBuilder();

        // DEFESA:
        // O DTO impede que a apresentação receba e altere diretamente a entidade persistente.
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
                reportService.generate(simulation.simulationId(), result.reportPath());
                return result;
            } catch (final Exception e) {
                simulation.markAsFailed(e.getMessage());
            }
        } else {
            simulation.markAsFailed(result.consoleOutput());
        }

        simulationRepository.save(simulation);

        return result;
    }

    public void submitSimulation(final String simulationId) {

        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.FLIGHT_CONTROL_OPERATOR,
                SEM4PIRoles.ADMIN);

        // DEFESA:
        // A submissão assíncrona evita bloquear a UI durante a execução do processo C.
        simulationJobService.submit(simulationId);
    }
}
