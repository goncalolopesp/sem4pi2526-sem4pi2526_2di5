package pt.isep.sem4pi.infrastructure.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isep.sem4pi.application.service.engineModel.EngineModelRegistrationService;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftType;
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

        registerAircraftModel("A320", "AIRBUS", AircraftType.PASSENGER, "A320", "TURBOFAN",
                new BigDecimal("42000.00"), new BigDecimal("77000.00"), new BigDecimal("62000.00"),
                new BigDecimal("24000.00"), new BigDecimal("39000.00"), new BigDecimal("828.00"),
                new BigDecimal("122.60"), new BigDecimal("0.0320"), new BigDecimal("1.5200"),
                new BigDecimal("6150.00"), new BigDecimal("9.45"), "CFM56-5B");

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

    private void registerAircraftModel(final String modelCode, final String manufacturerCode,
                                       final AircraftType aircraftType, final String modelName,
                                       final String motorization, final BigDecimal emptyWeight,
                                       final BigDecimal maximumTakeoffWeight, final BigDecimal maximumZeroFuelWeight,
                                       final BigDecimal maximumFuelCapacity, final BigDecimal serviceCeiling,
                                       final BigDecimal cruiseSpeed, final BigDecimal wingArea,
                                       final BigDecimal dragCoefficient, final BigDecimal liftCoefficient,
                                       final BigDecimal maximumRange, final BigDecimal aspectRatio,
                                       final String engineModelCode) {
        final AircraftModelId aircraftModelId = new AircraftModelId(modelCode);
        if (aircraftModelRepository.containsOfIdentity(aircraftModelId)) {
            return;
        }

        final AircraftModel aircraftModel = new AircraftModel(
                aircraftModelId,
                new pt.isep.sem4pi.domain.model.manufacture.ManufacturerId(manufacturerCode),
                aircraftType,
                modelName,
                motorization,
                emptyWeight,
                maximumTakeoffWeight,
                maximumZeroFuelWeight,
                maximumFuelCapacity,
                serviceCeiling,
                cruiseSpeed,
                wingArea,
                dragCoefficient,
                liftCoefficient,
                maximumRange,
                aspectRatio
        );

        final EngineModel engineModel = engineModelRepository.ofIdentity(new EngineModelId(engineModelCode)).orElse(null);
        if (engineModel != null) {
            aircraftModel.certifyEngine(engineModel);
        }

        aircraftModelRepository.save(aircraftModel);
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