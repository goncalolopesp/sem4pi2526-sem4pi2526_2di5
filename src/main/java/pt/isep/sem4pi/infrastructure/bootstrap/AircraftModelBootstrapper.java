package pt.isep.sem4pi.infrastructure.bootstrap;

import eapli.framework.actions.Action;
import pt.isep.sem4pi.application.AddAircraftModelController;
import pt.isep.sem4pi.domain.model.Manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.math.BigDecimal;
import java.util.List;

public class AircraftModelBootstrapper implements Action {

    @Override
    public boolean execute() {
        AddAircraftModelController controller = new AddAircraftModelController();

        try {
            System.out.println("Bootstrapping Aircraft Models...");

            Iterable<Manufacturer> manufacturers = controller.getManufacturers();
            Iterable<EngineModel> engines = controller.getEngineModels();

            /* When US056 is done, add this:
            controller.addAircraftModel(
                    "A320", airbus, "PASSENGER", "A320", MotorizationType.TURBOFAN,
                    new BigDecimal("42600"), new BigDecimal("61000"), new BigDecimal("73500"),
                    new BigDecimal("24210"), new BigDecimal("39000"), new BigDecimal("448"),
                    new BigDecimal("122.4"), new BigDecimal("0.024"), new BigDecimal("0.5"),
                    new BigDecimal("6100"), new BigDecimal("9.5"), engineIds
            );
            System.out.println("A320 Bootstrapped successfully!");
            */

            return true;
        } catch (Exception e) {
            System.out.println("Failed to bootstrap Aircraft Models: " + e.getMessage());
            return false;
        }
    }
}