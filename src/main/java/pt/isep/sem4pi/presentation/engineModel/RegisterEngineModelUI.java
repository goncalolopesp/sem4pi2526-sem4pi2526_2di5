package pt.isep.sem4pi.presentation.engineModel;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.engineModel.RegisterEngineModelController;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.math.BigDecimal;

@SuppressWarnings("squid:S106")
public class RegisterEngineModelUI extends AbstractUI {
    private final RegisterEngineModelController controller = new RegisterEngineModelController();

    @Override
    protected boolean doShow() {
        final String modelCode = Console.readNonEmptyLine("Engine model code:", "Code is required.");
        final String manufacturerCode = Console.readNonEmptyLine("Manufacturer code:", "Manufacturer is required.");
        final String name = Console.readNonEmptyLine("Engine model name:", "Name is required.");
        final String type = Console.readNonEmptyLine("Engine type:", "Type is required.");
        final BigDecimal power = readPositiveDecimal("Engine power:");
        final String fuelType = Console.readNonEmptyLine("Fuel type:", "Fuel type is required.");
        final BigDecimal efficiency = readPositiveDecimal("Engine efficiency:");

        try {
            final EngineModel engineModel =
                    controller.registerEngineModel(modelCode, manufacturerCode, name, type, power, fuelType, efficiency);
            System.out.println("Engine model registered successfully: " + engineModel.identity());
            return true;
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Register Engine Model";
    }

    private BigDecimal readPositiveDecimal(final String label) {
        while (true) {
            final String rawValue = Console.readNonEmptyLine(label, "A numeric value is required.");
            try {
                final BigDecimal value = new BigDecimal(rawValue);
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Value must be greater than zero.");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number format.");
            }
        }
    }
}
