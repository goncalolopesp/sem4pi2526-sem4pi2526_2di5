package pt.isep.sem4pi.infrastructure.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isep.sem4pi.application.service.engineModel.EngineModelRegistrationService;
import pt.isep.sem4pi.domain.model.aircraftModel.*;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.CustomerCollaboratorBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;

import java.math.BigDecimal;

import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;

public class BackofficeConfigurationBootstrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackofficeConfigurationBootstrapper.class);

    private final ManufacturerRepository manufacturerRepository = PersistenceContext.repositories().manufacturers();
    private final EngineModelRepository engineModelRepository = PersistenceContext.repositories().engineModels();
    private final AircraftModelRepository aircraftModelRepository = PersistenceContext.repositories().aircraftModels();
    private final AirControlAreaRepository airControlAreaRepository = PersistenceContext.repositories().airControlAreas();
    private final EngineModelRegistrationService engineModelRegistrationService =
            new EngineModelRegistrationService(engineModelRepository, manufacturerRepository);

    public boolean execute() {
        // Inicializar Manufacturers e Engine Models
        registerManufacturer("CFM", "CFM International", "France");
        registerManufacturer("RR", "Rolls-Royce", "United Kingdom");
        registerManufacturer("GE", "General Electric", "United States");
        registerManufacturer("AIRBUS", "Airbus", "France");

        registerEngineModel("CFM56-5B", "CFM", "CFM56-5B", EngineType.TURBOFAN,
                new BigDecimal("120.00"), FuelType.JET_A1, new BigDecimal("37.50"));
        registerEngineModel("TRENT1000", "RR", "Trent 1000", EngineType.TURBOFAN,
                new BigDecimal("330.00"), FuelType.JET_A1, new BigDecimal("41.20"));
        registerEngineModel("GENX-1B", "GE", "GEnx-1B", EngineType.TURBOFAN,
                new BigDecimal("330.00"), FuelType.JET_A1, new BigDecimal("40.80"));

        registerAircraftModel(
                "AIRBUS",                                // manufacturerCode
                AircraftType.PASSENGER,                  // aircraftType
                "A320",                                  // modelName
                MotorizationType.TURBOFAN,               // motorization (enum)
                new BigDecimal("42000.00"),              // emptyWeight
                new BigDecimal("62000.00"),              // mzfw  ← confirma ordem
                new BigDecimal("77000.00"),              // mtow  ← confirma ordem
                new BigDecimal("24000.00"),              // fuelCapacity
                new BigDecimal("39000.00"),              // serviceCeiling
                new BigDecimal("828.00"),                // cruiseSpeed
                new BigDecimal("122.60"),                // wingArea
                new BigDecimal("0.0320"),                // dragCoefficient
                new BigDecimal("1.5200"),                // liftCoefficient
                new BigDecimal("6150.00"),               // maximumRange
                new BigDecimal("9.45"),                  // aspectRatio
                "CFM56-5B"                               // engineModelCode
        );

        // Inicializar Air Control Areas (Critério de Aceitação 3)
        registerAirControlArea("LIS_FIR", 36.0, 42.0, -10.0, -6.0, "Lisbon FIR", 1000.0);
        registerAirControlArea("MAD_FIR", 35.0, 43.0, -5.0, 0.0, "Madrid FIR", 1200.0);

        //Executar o Bootstrap dos Customer Collaborators
        CustomerCollaboratorBootstrapper ccb = new CustomerCollaboratorBootstrapper();
        ccb.execute();

        return true;
    }

    private void registerManufacturer(final String code, final String name, final String country) {
        final ManufacturerId manufacturerId = new ManufacturerId(code);
        if (!manufacturerRepository.containsOfIdentity(manufacturerId)) {
            manufacturerRepository.save(new Manufacturer(manufacturerId, name, country));
        }
    }

    private void registerEngineModel(final String modelCode, final String manufacturerCode, final String name,
                                     final EngineType type, final BigDecimal power, final FuelType fuelType,
                                     final BigDecimal efficiency) {
        final ManufacturerId manufacturerId = new ManufacturerId(manufacturerCode);
        if (!engineModelRepository.containsOfIdentity(new pt.isep.sem4pi.domain.model.engineModel.EngineModelId(modelCode))
                && !engineModelRepository.existsWithManufacturerAndName(manufacturerId, name)) {
            engineModelRegistrationService.register(modelCode, manufacturerCode, name, type, power, fuelType, efficiency);
        }
    }

    private void registerAircraftModel(final String manufacturerCode,
                                       final AircraftType aircraftType,
                                       final String modelName,
                                       final MotorizationType motorization,
                                       final BigDecimal emptyWeight,
                                       final BigDecimal mzfw,
                                       final BigDecimal mtow,
                                       final BigDecimal fuelCapacity,
                                       final BigDecimal serviceCeiling,
                                       final BigDecimal cruiseSpeed,
                                       final BigDecimal wingArea,
                                       final BigDecimal dragCoefficient,
                                       final BigDecimal liftCoefficient,
                                       final BigDecimal maximumRange,
                                       final BigDecimal aspectRatio,
                                       final String engineModelCode) {

        final ManufacturerId manufacturerId = new ManufacturerId(manufacturerCode);
        final AircraftModelId aircraftModelId = new AircraftModelId(manufacturerId, modelName);

        if (aircraftModelRepository.containsOfIdentity(aircraftModelId)) {
            LOGGER.info("Aircraft Model '{}' already exists.", aircraftModelId);
            return;
        }

        // Get the engine model BEFORE building the aircraft (the constructor requires at least one)
        final EngineModel engineModel = engineModelRepository
                .ofIdentity(new EngineModelId(engineModelCode))
                .orElse(null);

        if (engineModel == null) {
            LOGGER.warn("Engine Model '{}' not found. Skipping aircraft '{}' bootstrap.",
                    engineModelCode, modelName);
            return;
        }

        // Build the value objects expected by the constructor
        final Weight weight = new Weight(emptyWeight, mzfw, mtow);
        final Fuel fuel = new Fuel(fuelCapacity);
        final AircraftPerformance performance = new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange);
        final WingGeometry wingGeometry = new WingGeometry(wingArea, aspectRatio);
        final Coefficient aerodynamics = new Coefficient(dragCoefficient, liftCoefficient);

        final AircraftModel aircraftModel = new AircraftModel(
                aircraftModelId,
                aircraftType,
                motorization,
                weight,
                aerodynamics,
                performance,
                fuel,
                wingGeometry,
                java.util.List.of(engineModel)
        );

        aircraftModelRepository.save(aircraftModel);
        LOGGER.info("Aircraft Model '{}' was successfully registered.", aircraftModelId);
    }

    private void registerAirControlArea(String code, double minLat, double maxLat, double minLon, double maxLon, String name, double minFuel) {
        try {
            AirControlAreaID id = new AirControlAreaID(code);
            if (airControlAreaRepository.containsOfIdentity(id)) {
                LOGGER.info("Air Control Area '{}' already exists in the system.", code);
                return;
            }

            AirControlAreaBoundary boundaries = new AirControlAreaBoundary(minLat, maxLat, minLon, maxLon);
            AirControlArea newArea = new AirControlArea(code, boundaries, name, minFuel);

            airControlAreaRepository.save(newArea);
            LOGGER.info("Air Control Area '{}' was successfully registered.", code);

        } catch (final ConcurrencyException | IntegrityViolationException e) {
            LOGGER.warn("Integrity violation registering '{}'", code);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error registering Air Control Area '{}': {}", code, e.getMessage());
        }
    }
}