package pt.isep.sem4pi.application.service.aircraftModel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import pt.isep.sem4pi.domain.model.aircraftModel.*;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AddAircraftModelService {

    private final AuthorizationService authz;
    private final AircraftModelRepository aircraftModelRepository;
    private final EngineModelRepository engineModelRepository;

    public AddAircraftModelService(AuthorizationService authz, AircraftModelRepository aircraftModelRepository, EngineModelRepository engineModelRepository) {
        this.authz = authz;
        this.aircraftModelRepository = aircraftModelRepository;
        this.engineModelRepository = engineModelRepository;
    }

    public AircraftModel addAircraftModel(Manufacturer manufacturer, String type, String name,
                                          EngineType motorization, BigDecimal emptyWeight, BigDecimal mzfw, BigDecimal mtow,
                                          BigDecimal fuelCap, BigDecimal ceiling, BigDecimal speed,
                                          BigDecimal wingArea, BigDecimal drag, BigDecimal lift,
                                          BigDecimal range, BigDecimal aspectRatio, List<EngineModel> engine,
                                          Integer maximumCapacity) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);

        ManufacturerId manId = manufacturer.identity();
        AircraftModelId modelId = new AircraftModelId(manId, name);

        if (aircraftModelRepository.containsOfIdentity(modelId)) {
            throw new IllegalArgumentException("Already exists a model with this name for this manufacturer.");
        }

        // Fetch fresh and attached instances from the database
        List<EngineModel> attachedEngines = new ArrayList<>();
        for (EngineModel e : engine) {
            EngineModel realEngine = engineModelRepository.ofIdentity(e.identity())
                    .orElseThrow(() -> new IllegalArgumentException("Engine not found in database."));
            attachedEngines.add(realEngine);
        }

        AircraftType aircraftType = AircraftType.valueOf(type.toUpperCase());

        Weight weight = new Weight(emptyWeight, mzfw, mtow);
        Fuel fuel = new Fuel(fuelCap);
        AircraftPerformance performance = new AircraftPerformance(ceiling, speed, range);
        WingGeometry geometry = new WingGeometry(wingArea, aspectRatio);
        Coefficient aerodynamics = new Coefficient(drag, lift);

        // Pass the attached engines here
        return new AircraftModel(modelId, aircraftType, motorization,
                weight, aerodynamics, performance, fuel, geometry, attachedEngines, maximumCapacity);
    }
}