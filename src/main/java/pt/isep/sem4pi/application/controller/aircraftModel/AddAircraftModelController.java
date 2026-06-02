package pt.isep.sem4pi.application.controller.aircraftModel;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.service.aircraftModel.AddAircraftModelService;
import pt.isep.sem4pi.application.viadto.AircraftModelDTORepresentationBuilder;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.AircraftModelDTO;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.aircraftModel.*;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller to add a new Aircraft Model to the system. Only users with BACKOFFICE_OPERATOR and ADMIN role can perform this action.
 * The controller interacts with the repositories to fetch necessary data and save the new Aircraft Model.
 */

@UseCaseController
public class AddAircraftModelController {

    private final AuthorizationService authz;
    private final AircraftModelRepository aircraftRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final EngineModelRepository engineRepo;
    private final AddAircraftModelService  addAircraftModelService;

    // Constructor for production (uses EAPLI factories)
    public AddAircraftModelController() {
        this.authz = AuthzRegistry.authorizationService();
        this.aircraftRepo = PersistenceContext.repositories().aircraftModels();
        this.manufacturerRepo = PersistenceContext.repositories().manufacturers();
        this.engineRepo = PersistenceContext.repositories().engineModels();

        // Pass engineRepo to the service
        this.addAircraftModelService = new AddAircraftModelService(authz, aircraftRepo, engineRepo);
    }

    // Constructor for testing (allows Mockito to inject fake repositories)
    public AddAircraftModelController(AuthorizationService authz, AircraftModelRepository aircraftRepo,
                                      ManufacturerRepository manufacturerRepo, EngineModelRepository engineRepo, AddAircraftModelService addAircraftModelService) {
        this.authz = authz;
        this.aircraftRepo = aircraftRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.engineRepo = engineRepo;
        this.addAircraftModelService = addAircraftModelService;
    }

    public AircraftModelDTO addAircraftModel(Manufacturer manufacturer, String type, String name,
                                             EngineType motorization, BigDecimal emptyWeight, BigDecimal mzfw, BigDecimal mtow,
                                             BigDecimal fuelCap, BigDecimal ceiling, BigDecimal speed,
                                             BigDecimal wingArea, BigDecimal drag, BigDecimal lift,
                                             BigDecimal range, BigDecimal aspectRatio, List<EngineModel> engine,
                                             Integer maximumCapacity) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);

        AircraftModel aircraftModel = addAircraftModelService.addAircraftModel(manufacturer, type, name, motorization, emptyWeight, mzfw, mtow, fuelCap, ceiling, speed, wingArea, drag, lift, range, aspectRatio, engine, maximumCapacity);

        AircraftModel savedAircraft = aircraftRepo.save(aircraftModel);

        AircraftModelDTORepresentationBuilder builder = new AircraftModelDTORepresentationBuilder();

        AircraftModelDTO dto = aircraftModel.buildRepresentation(builder);

        dto.setVersion(savedAircraft.version());

        return dto;
    }

    public Iterable<Manufacturer> getManufacturers() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return manufacturerRepo.findAll();
    }

    public Iterable<EngineModel> getEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return engineRepo.findAll();
    }
}