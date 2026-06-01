package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;

import java.math.BigDecimal;
import java.util.Collections;

public class AircraftModelCertificationTest {

    @Test
    public void certifyEngineAddsCompatibleEngine() {
        final AircraftModel aircraftModel = buildAircraftModel("TURBOFAN");
        final EngineModel engineModel = buildEngineModel("CFM56-5B", EngineType.TURBOFAN);

        aircraftModel.certifyEngine(engineModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void certifyEngineRejectsDuplicateEngine() {
        final AircraftModel aircraftModel = buildAircraftModel("TURBOFAN");
        final EngineModel engineModel = buildEngineModel("CFM56-5B", EngineType.TURBOFAN);

        aircraftModel.certifyEngine(engineModel);
        aircraftModel.certifyEngine(engineModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void certifyEngineRejectsIncompatibleEngine() {
        final AircraftModel aircraftModel = buildAircraftModel("TURBOFAN");
        final EngineModel engineModel = buildEngineModel("PW150", EngineType.TURBOPROP);

        aircraftModel.certifyEngine(engineModel);
    }

    private AircraftModel buildAircraftModel(final String motorization) {
        // Build an initial engine to satisfy the AircraftModel constructor preconditions
        final EngineModel initEngine = buildEngineModel("INIT-ENG", EngineType.valueOf(motorization));

        return new AircraftModel(
                new AircraftModelId(new ManufacturerId("AIRBUS"), "A320"),
                AircraftType.PASSENGER,
                EngineType.valueOf(motorization),
                new Weight(new BigDecimal("42000.00"), new BigDecimal("62000.00"), new BigDecimal("77000.00")),
                new Coefficient(new BigDecimal("0.0320"), new BigDecimal("1.5200")),
                new AircraftPerformance(new BigDecimal("39000.00"), new BigDecimal("828.00"), new BigDecimal("6150.00")),
                new Fuel(new BigDecimal("24000.00")),
                new WingGeometry(new BigDecimal("122.60"), new BigDecimal("9.45")),
                Collections.singletonList(initEngine),
                180
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