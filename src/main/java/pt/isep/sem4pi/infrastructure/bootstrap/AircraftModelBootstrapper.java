package pt.isep.sem4pi.infrastructure.bootstrap;

import eapli.framework.actions.Action;
import pt.isep.sem4pi.application.AddAircraftModelController;
import pt.isep.sem4pi.domain.model.aircraftModel.MotorizationType;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrapper class for initializing AircraftModel data.
 * <p>This class implements the Action interface and is responsible for bootstrapping
 * aircraft models into the system during application startup. It uses the
 * AddAircraftModelController to add predefined aircraft models.</p>
 */
public class AircraftModelBootstrapper implements Action {

    /**
     * Executes the bootstrapping process for aircraft models.
     * <p>This method attempts to bootstrap aircraft models by retrieving manufacturers
     * and engine models, then adding aircraft models. Currently, the actual addition
     * is commented out pending completion of related user stories.</p>
     *
     * @return true if the bootstrapping succeeds, false otherwise
     */
    @Override
    public boolean execute() {
        AddAircraftModelController controller = new AddAircraftModelController();

        try {
            System.out.println("Bootstrapping Aircraft Models...");

            Iterable<Manufacturer> manufacturers = controller.getManufacturers();
            Iterable<EngineModel> engines = controller.getEngineModels();

            Manufacturer airbus = null;
            for (Manufacturer m : manufacturers) {
                if (m.identity().toString().equalsIgnoreCase("CFM")) {
                    airbus = m;
                    break;
                }
            }

            if (airbus == null) {
                System.out.println("Bootstrapper Error: Manufacturer AIRBUS not found. The Manufacturers bootstrapper (US056) must run first!");
                return false;
            }

            List<EngineModelId> engineIds = new ArrayList<>();
            for (EngineModel e : engines) {
                if (e.identity().toString().equalsIgnoreCase("CFM56-5B")) {
                    engineIds.add(e.identity());
                }
            }

            if (engineIds.isEmpty()) {
                System.out.println("Bootstrapper Warning: None compatible engine found. US56 must create the engine 'CFM56-5B'.");
                return false;
            }

            controller.addAircraftModel(
                    airbus,
                    "PASSENGER",
                    "A320",
                    MotorizationType.TURBOFAN,
                    new BigDecimal("42600"), // emptyWeight
                    new BigDecimal("61000"), // mzfw
                    new BigDecimal("73500"), // mtow
                    new BigDecimal("24210"), // fuelCap
                    new BigDecimal("39000"), // ceiling
                    new BigDecimal("448"),   // speed
                    new BigDecimal("122.4"), // wingArea
                    new BigDecimal("0.024"), // drag
                    new BigDecimal("0.5"),   // lift
                    new BigDecimal("6100"),  // range
                    new BigDecimal("9.5"),   // aspectRatio
                    engineIds
            );
            System.out.println("A320 Bootstrapped successfully!");


            return true;
        } catch (Exception e) {
            System.out.println("Failed to bootstrap Aircraft Models: " + e.getMessage());
            return false;
        }
    }
}