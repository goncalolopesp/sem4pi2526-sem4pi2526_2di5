package pt.isep.sem4pi.application.service.simulation;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RunAreaSimulationService {

    private final AuthorizationService authz;

    public RunAreaSimulationService(final AuthorizationService authz) {
        this.authz = authz;
    }

    public Simulation createSimulation(final String airControlAreaId,
                                       final LocalDateTime startTime,
                                       final LocalDateTime endTime,
                                       final Integer timeStep,
                                       final Double safetyThreshold,
                                       final List<String> flightPlanIds) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.FLIGHT_CONTROL_OPERATOR, SEM4PIRoles.ADMIN);

        final String simulationId = "SIM-" + UUID.randomUUID();

        final Simulation simulation = new Simulation(
                simulationId,
                airControlAreaId,
                startTime,
                endTime,
                timeStep,
                safetyThreshold
        );

        for (final String flightPlanId : flightPlanIds) {
            simulation.addFlightPlan(flightPlanId);
        }

        return simulation;
    }
}