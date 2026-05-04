package pt.isep.sem4pi.application;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.domain.model.Manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.*;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

import java.math.BigDecimal;
import java.util.List;

@UseCaseController
public class AddAircraftModelController {

    private final AuthorizationService authz;
    private final AircraftModelRepository aircraftRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final EngineModelRepository engineRepo;

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
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR);
        return manufacturerRepo.findAll();
    }

    public Iterable<EngineModel> getEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR);
        return engineRepo.findAll();
    }

    public AircraftModel addAircraftModel(String id, Manufacturer manufacturer, String type, String name,
                                          MotorizationType motorization, BigDecimal emptyWeight, BigDecimal mzfw, BigDecimal mtow,
                                          BigDecimal fuelCap, BigDecimal ceiling, BigDecimal speed,
                                          BigDecimal wingArea, BigDecimal drag, BigDecimal lift,
                                          BigDecimal range, BigDecimal aspectRatio, List<EngineModelId> engineIds) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR);

        if (aircraftRepo.containsOfManufacturerAndName(manufacturer.identity(), name)) {
            throw new IllegalArgumentException("This Manufacturer already has an Aircraft Model with the name: " + name);
        }

        AircraftModelId modelId = new AircraftModelId(id);
        ManufacturerId manId = manufacturer.identity();
        AircraftType aircraftType = AircraftType.valueOf(type.toUpperCase());

        Weight weight = new Weight(emptyWeight, mzfw, mtow);
        Fuel fuel = new Fuel(fuelCap);
        AircraftPerformance performance = new AircraftPerformance(ceiling, speed, range);
        WingGeometry geometry = new WingGeometry(wingArea, aspectRatio);
        Coefficient aerodynamics = new Coefficient(drag, lift);

        AircraftModel newAircraft = new AircraftModel(modelId, manId, aircraftType, name, motorization,
                weight, aerodynamics, performance, fuel, geometry, engineIds);

        return aircraftRepo.save(newAircraft);
    }
}