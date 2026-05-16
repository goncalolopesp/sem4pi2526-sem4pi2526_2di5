package pt.isep.sem4pi.domain.model.aircraftModel;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;

import java.math.BigDecimal;

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
        return new AircraftModel(
                new AircraftModelId("A320"),
                new ManufacturerId("AIRBUS"),
                AircraftType.PASSENGER,
                "A320",
                motorization,
                new BigDecimal("42000.00"),
                new BigDecimal("77000.00"),
                new BigDecimal("62000.00"),
                new BigDecimal("24000.00"),
                new BigDecimal("39000.00"),
                new BigDecimal("828.00"),
                new BigDecimal("122.60"),
                new BigDecimal("0.0320"),
                new BigDecimal("1.5200"),
                new BigDecimal("6150.00"),
                new BigDecimal("9.45")
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

