package pt.isep.sem4pi.application.controller.aircraftModel;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
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
 * Controller to add a new Aircraft Model to the system. Only users with BACKOFFICE_OPERATOR role can perform this action.
 * The controller interacts with the repositories to fetch necessary data and save the new Aircraft Model.
 */

@UseCaseController
public class AddAircraftModelController {

    private final AuthorizationService authz;
    private final AircraftModelRepository aircraftRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final EngineModelRepository engineRepo;

    private ManufacturerId airbus = new ManufacturerId("AIRBUS");
    private ManufacturerId boeing = new ManufacturerId("BOEING");

    // Constructor for production (uses EAPLI factories)
    public AddAircraftModelController() {
        this.authz = AuthzRegistry.authorizationService();
        this.aircraftRepo = PersistenceContext.repositories().aircraftModels();
        this.manufacturerRepo = PersistenceContext.repositories().manufacturers();
        this.engineRepo = PersistenceContext.repositories().engineModels();
    }

    // Constructor for testing (allows Mockito to inject fake repositories)
    public AddAircraftModelController(AuthorizationService authz, AircraftModelRepository aircraftRepo,
                                      ManufacturerRepository manufacturerRepo, EngineModelRepository engineRepo) {
        this.authz = authz;
        this.aircraftRepo = aircraftRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.engineRepo = engineRepo;
    }

    public Iterable<Manufacturer> getManufacturers() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return manufacturerRepo.findAll();
    }

    public Iterable<EngineModel> getEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
        return engineRepo.findAll();
    }

    public AircraftModel addAircraftModel(Manufacturer manufacturer, String type, String name,
                                          EngineType motorization, BigDecimal emptyWeight, BigDecimal mzfw, BigDecimal mtow,
                                          BigDecimal fuelCap, BigDecimal ceiling, BigDecimal speed,
                                          BigDecimal wingArea, BigDecimal drag, BigDecimal lift,
                                          BigDecimal range, BigDecimal aspectRatio, List<EngineModel> engine) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);

        ManufacturerId manId = manufacturer.identity();
        AircraftModelId modelId = new AircraftModelId(manId, name);

        if (aircraftRepo.containsOfIdentity(modelId)) {
            throw new IllegalArgumentException("Already exists a model with this name for this manufacturer.");
        }

        AircraftType aircraftType = AircraftType.valueOf(type.toUpperCase());

        Weight weight = new Weight(emptyWeight, mzfw, mtow);
        Fuel fuel = new Fuel(fuelCap);
        AircraftPerformance performance = new AircraftPerformance(ceiling, speed, range);
        WingGeometry geometry = new WingGeometry(wingArea, aspectRatio);
        Coefficient aerodynamics = new Coefficient(drag, lift);

        AircraftModel newAircraft = new AircraftModel(modelId, aircraftType, motorization,
                weight, aerodynamics, performance, fuel, geometry, engine);

        return aircraftRepo.save(newAircraft);
    }
}