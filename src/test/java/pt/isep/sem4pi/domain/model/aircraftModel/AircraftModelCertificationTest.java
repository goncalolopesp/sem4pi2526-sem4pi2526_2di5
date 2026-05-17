package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;

import java.math.BigDecimal;
import java.util.List;

public class AircraftModelCertificationTest {

    @Test
    public void certifyEngineAddsCompatibleEngine() {
        final EngineModel firstEngine = buildEngineModel("CFM56-5B", EngineType.TURBOFAN);
        final AircraftModel aircraftModel = buildAircraftModel(MotorizationType.TURBOFAN, firstEngine);

        // Add a different compatible engine
        final EngineModel anotherEngine = buildEngineModel("CFM56-5C", EngineType.TURBOFAN);
        aircraftModel.certifyEngine(anotherEngine);
    }

    @Test(expected = IllegalArgumentException.class)
    public void certifyEngineRejectsDuplicateEngine() {
        final EngineModel engineModel = buildEngineModel("CFM56-5B", EngineType.TURBOFAN);
        final AircraftModel aircraftModel = buildAircraftModel(MotorizationType.TURBOFAN, engineModel);

        // Engine already certified through the constructor; certifying again must fail
        aircraftModel.certifyEngine(engineModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void certifyEngineRejectsIncompatibleEngine() {
        final EngineModel compatibleEngine = buildEngineModel("CFM56-5B", EngineType.TURBOFAN);
        final AircraftModel aircraftModel = buildAircraftModel(MotorizationType.TURBOFAN, compatibleEngine);

        final EngineModel incompatibleEngine = buildEngineModel("PW150", EngineType.TURBOPROP);
        aircraftModel.certifyEngine(incompatibleEngine);
    }

    private AircraftModel buildAircraftModel(final MotorizationType motorization, final EngineModel initialEngine) {
        final AircraftModelId id = new AircraftModelId(new ManufacturerId("AIRBUS"), "A320");

        final Weight weight = new Weight(
                new BigDecimal("42000.00"),  // emptyWeight
                new BigDecimal("62000.00"),  // mzfw
                new BigDecimal("77000.00")   // mtow
        );
        final Fuel fuel = new Fuel(new BigDecimal("24000.00"));
        final AircraftPerformance performance = new AircraftPerformance(
                new BigDecimal("39000.00"),  // ceiling
                new BigDecimal("828.00"),    // speed
                new BigDecimal("6150.00")    // range
        );
        final WingGeometry wingGeometry = new WingGeometry(
                new BigDecimal("122.60"),    // wingArea
                new BigDecimal("9.45")       // aspectRatio
        );
        final Coefficient aerodynamics = new Coefficient(
                new BigDecimal("0.0320"),    // drag
                new BigDecimal("1.5200")     // lift
        );

        return new AircraftModel(
                id,
                AircraftType.PASSENGER,
                motorization,
                weight,
                aerodynamics,
                performance,
                fuel,
                wingGeometry,
                List.of(initialEngine)
        );
    }

    private EngineModel buildEngineModel(final String code, final EngineType type) {
        final Manufacturer manufacturer = new Manufacturer(new ManufacturerId("CFM"), "CFM", "France");
        return new EngineModel(
                new EngineModelId(code),
                manufacturer,
                code,
                type,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );
    }
}