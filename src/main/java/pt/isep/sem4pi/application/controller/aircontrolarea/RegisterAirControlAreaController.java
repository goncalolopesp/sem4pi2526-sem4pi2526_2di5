package pt.isep.sem4pi.application.controller.aircontrolarea;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.aircontrolarea.DTO.AirControlAreaCreateDTO;
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

    public AirControlArea registerAirControlArea(AirControlAreaCreateDTO dto) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);

        AirControlAreaID id = new AirControlAreaID(dto.getCode());
        if (repository.containsOfIdentity(id)) {
            throw new IllegalArgumentException("An Air Control Area with the code '" + dto.getCode() + "' already exists in the system.");
        }

        AirControlAreaBoundary boundaries = new AirControlAreaBoundary(
                dto.getMinLatitude(), dto.getMaxLatitude(), dto.getMinLongitude(), dto.getMaxLongitude());
        AirControlArea newArea = new AirControlArea(
                dto.getCode(), boundaries, dto.getName(), dto.getMinimumReserveFuel());

        return repository.save(newArea);
    }

    public AirControlArea registerAirControlArea(String code, double minLat, double maxLat, double minLon, double maxLon, String name, double minFuel) {
        AirControlAreaCreateDTO dto = new AirControlAreaCreateDTO(code, name, minLat, maxLat, minLon, maxLon, minFuel);
        return registerAirControlArea(dto);
    }
}