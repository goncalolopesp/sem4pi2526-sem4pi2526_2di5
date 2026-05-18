package pt.isep.sem4pi.application.controller.aircraft;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.aircraft.AircraftRetirementService;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

public class RetireAircraftController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final AircraftRetirementService retirementService;

    public RetireAircraftController() {
        this.aircraftRepository = PersistenceContext.repositories().aircrafts();
        this.flightRepository = PersistenceContext.repositories().flights();
        this.retirementService = new AircraftRetirementService(aircraftRepository, flightRepository);
    }

    /**
     * Garante permissao e delega a retirada da aeronave.
     */
    public Aircraft retireAircraft(final AircraftRegistration registration) {
        authz.ensureAuthenticatedUserHasAnyOf(
                SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                SEM4PIRoles.ADMIN
        );
        return retirementService.retireAircraft(registration);
    }

    /**
     * Lista aeronaves para selecao na UI.
     */
    public List<Aircraft> listAircraft() {
        final List<Aircraft> aircraft = new ArrayList<>();
        for (final Aircraft item : aircraftRepository.findAll()) {
            aircraft.add(item);
        }
        return aircraft;
    }
}
