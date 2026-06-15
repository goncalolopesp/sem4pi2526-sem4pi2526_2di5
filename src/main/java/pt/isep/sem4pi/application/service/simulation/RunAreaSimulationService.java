package pt.isep.sem4pi.application.service.simulation;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import pt.isep.sem4pi.domain.model.simulation.Simulation;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Prepara a simulação da US100.
 * Organiza os dados e deixa as regras principais na entidade Simulation.
 */
public class RunAreaSimulationService {

    private final AuthorizationService authz;

    public RunAreaSimulationService(final AuthorizationService authz) {
        this.authz = authz;
    }

    /**
     * Cria a simulação e associa os planos de voo escolhidos pelo FCO.
     * Depois, o controller guarda a simulação.
     */
    public Simulation createSimulation(final String airControlAreaId, final Double minLat, final Double maxLat, final Double minLon, final Double maxLon, final Integer startSecond, final Integer endSecond, final Integer timeStep, final String weather, final Double safetyMinSepNm, final Double safetyMinAltM, final Integer violationThreshold, final List<String> flightPlanIds) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.FLIGHT_CONTROL_OPERATOR, SEM4PIRoles.ADMIN);

        final String simulationId = "SIM-" + UUID.randomUUID();

        final Simulation simulation = new Simulation(simulationId, airControlAreaId, LocalDateTime.now(), LocalDateTime.now().plusSeconds(endSecond - startSecond), timeStep, safetyMinSepNm, minLat, maxLat, minLon, maxLon, startSecond, endSecond, weather, safetyMinSepNm, safetyMinAltM, violationThreshold);

        for (final String flightPlanId : flightPlanIds) {
            simulation.addFlightPlan(flightPlanId);
        }

        return simulation;
    }
}
