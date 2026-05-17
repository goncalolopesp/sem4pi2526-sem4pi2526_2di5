package pt.isep.sem4pi.application.controller.aircontrolarea;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.domain.aircontrolarea.*;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

@UseCaseController
public class RegisterAirControlAreaController {

    private final AuthorizationService authz;
    private final AirControlAreaRepository repository;

    public RegisterAirControlAreaController() {
        this.authz = AuthzRegistry.authorizationService();
        this.repository = PersistenceContext.repositories().airControlAreas();
    }

    public RegisterAirControlAreaController(final AirControlAreaRepository repository, final AuthorizationService authz) {
        this.authz = authz;
        this.repository = repository;
    }

    public AirControlArea registerAirControlArea(String code, double minLat, double maxLat, double minLon, double maxLon, String name, double minFuel) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);

        AirControlAreaID id = new AirControlAreaID(code);
        if (repository.containsOfIdentity(id)) {
            throw new IllegalArgumentException("An Air Control Area with the code '" + code + "' already exists in the system.");
        }

        AirControlAreaBoundary boundaries = new AirControlAreaBoundary(minLat, maxLat, minLon, maxLon);
        AirControlArea newArea = new AirControlArea(code, boundaries, name, minFuel);

        return repository.save(newArea);
    }
}