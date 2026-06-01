package pt.isep.sem4pi.infrastructure.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isep.sem4pi.application.service.model.engineModel.EngineModelRegistrationService;
import pt.isep.sem4pi.domain.model.aircraftModel.*;
import pt.isep.sem4pi.domain.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.aircraft.Aircraft;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.flight.Flight;
import pt.isep.sem4pi.domain.flight.FlightId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.CompanyFleetBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.CustomerCollaboratorBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.FlightRouteBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaBoundary;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;

public class BackofficeConfigurationBootstrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackofficeConfigurationBootstrapper.class);

    private final ManufacturerRepository manufacturerRepository = PersistenceContext.repositories().manufacturers();
    private final EngineModelRepository engineModelRepository = PersistenceContext.repositories().engineModels();
    private final AircraftModelRepository aircraftModelRepository = PersistenceContext.repositories().aircraftModels();
    private final AirControlAreaRepository airControlAreaRepository = PersistenceContext.repositories().airControlAreas();
    private final AircraftRepository aircraftRepository = PersistenceContext.repositories().aircrafts();
    private final FlightRepository flightRepository = PersistenceContext.repositories().flights();
    private final EngineModelRegistrationService engineModelRegistrationService =
            new EngineModelRegistrationService(engineModelRepository, manufacturerRepository);

    public boolean execute() {
        registerManufacturer("CFM", "CFM International", "France");
        registerManufacturer("RR", "Rolls-Royce", "United Kingdom");
        registerManufacturer("GE", "General Electric", "United States");
        registerManufacturer("AIRBUS", "Airbus", "France");
        registerManufacturer("BOEING", "Boeing", "United States"); // NOVO FABRICANTE

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
                new BigDecimal("6150.00"), new BigDecimal("9.45"), "CFM56-5B", 180);

        registerAircraftModel("B737", "BOEING", AircraftType.PASSENGER, "B737", "TURBOFAN",
                new BigDecimal("41000.00"), new BigDecimal("70000.00"), new BigDecimal("60000.00"),
                new BigDecimal("20000.00"), new BigDecimal("41000.00"), new BigDecimal("450.00"),
                new BigDecimal("125.00"), new BigDecimal("0.0300"), new BigDecimal("1.5000"),
                new BigDecimal("5500.00"), new BigDecimal("9.00"), "CFM56-5B", 200);

        registerAircraftModel("B787", "BOEING", AircraftType.PASSENGER, "B787", "TURBOFAN",
                new BigDecimal("119000.00"), new BigDecimal("227000.00"), new BigDecimal("161000.00"),
                new BigDecimal("126000.00"), new BigDecimal("43000.00"), new BigDecimal("490.00"),
                new BigDecimal("325.00"), new BigDecimal("0.0250"), new BigDecimal("1.8000"),
                new BigDecimal("13000.00"), new BigDecimal("11.00"), "GENX-1B", 300);

        registerAircraft("CS-TST", "AIRBUS", "A320", 150, 20, 0, "Portugal", LocalDate.of(2015, 5, 10)); // Idade Média
        registerAircraft("CS-RET", "AIRBUS", "A320", 120, 10, 0, "Portugal", LocalDate.of(2020, 8, 20)); // Mais Novo
        registerAircraft("CS-B73", "BOEING", "B737", 180, 10, 0, "Portugal", LocalDate.of(2008, 2, 15)); // Mais Velho
        registerAircraft("CS-B78", "BOEING", "B787", 200, 40, 10, "Portugal", LocalDate.of(2023, 11, 1)); // Novíssimo

        registerFlight("TP100", "CS-TST");

        registerAirControlArea("LIS_FIR", 36.0, 42.0, -10.0, -6.0, "Lisbon FIR", 1000.0);
        registerAirControlArea("MAD_FIR", 35.0, 43.0, -5.0, 0.0, "Madrid FIR", 1200.0);

        CustomerCollaboratorBootstrapper ccb = new CustomerCollaboratorBootstrapper();
        ccb.execute();

        CompanyFleetBootstrapper fleetBootstrapper = new CompanyFleetBootstrapper();
        fleetBootstrapper.execute();

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
                                       final String engineModelCode, final Integer maximumCapacity) {
        final AircraftModelId aircraftModelId = new AircraftModelId(new ManufacturerId(manufacturerCode), modelCode);
        if (aircraftModelRepository.containsOfIdentity(aircraftModelId)) {
            return;
        }

        final EngineModel engineModel = engineModelRepository.ofIdentity(new EngineModelId(engineModelCode)).orElse(null);
        if (engineModel == null) {
            LOGGER.warn("Engine model '{}' not found. Skipping registration of aircraft model {}.", engineModelCode, aircraftModelId);
            return;
        }

        final EngineType motorizationEnum = EngineType.valueOf(motorization);

        final AircraftModel aircraftModel = new AircraftModel(
                aircraftModelId,
                aircraftType,
                motorizationEnum,
                new Weight(emptyWeight, maximumZeroFuelWeight, maximumTakeoffWeight),
                new Coefficient(dragCoefficient, liftCoefficient),
                new AircraftPerformance(serviceCeiling, cruiseSpeed, maximumRange),
                new Fuel(maximumFuelCapacity),
                new WingGeometry(wingArea, aspectRatio),
                Collections.singletonList(engineModel),
                maximumCapacity
        );

        aircraftModelRepository.save(aircraftModel);
    }

    private void registerAircraft(final String registrationCode, final String manufacturerCode, final String aircraftModelCode,
                                  final int eco, final int bus, final int first, final String country, final LocalDate manufacturingDate) {
        final AircraftRegistration registration = new AircraftRegistration(registrationCode);
        if (aircraftRepository.containsOfIdentity(registration)) {
            return;
        }

        final CabinConfiguration seats = new CabinConfiguration(eco, bus, first);
        final AircraftModelId modelId = new AircraftModelId(new ManufacturerId(manufacturerCode), aircraftModelCode);

        // Passa a data de fabrico para o construtor em vez do LocalDate.now()
        final Aircraft aircraft = new Aircraft(registration, modelId, seats, country, manufacturingDate);
        aircraftRepository.save(aircraft);
    }

    private void registerFlight(final String flightCode, final String registrationCode) {
        final FlightId flightId = new FlightId(flightCode);
        if (flightRepository.containsOfIdentity(flightId)) {
            return;
        }
        final Flight flight = new Flight(flightId, new AircraftRegistration(registrationCode));
        flightRepository.save(flight);
    }

    private void registerAirControlArea(String code, double minLat, double maxLat, double minLon, double maxLon, String name, double minFuel) {
        try {
            AirControlAreaID id = new AirControlAreaID(code);
            if (airControlAreaRepository.containsOfIdentity(id)) {
                return;
            }
            AirControlAreaBoundary boundaries = new AirControlAreaBoundary(minLat, maxLat, minLon, maxLon);
            AirControlArea newArea = new AirControlArea(code, boundaries, name, minFuel);
            airControlAreaRepository.save(newArea);
        } catch (Exception e) {
            LOGGER.error("Unexpected error registering Air Control Area '{}': {}", code, e.getMessage());
        }
    }
}