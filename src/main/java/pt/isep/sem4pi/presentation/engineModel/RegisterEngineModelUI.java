package pt.isep.sem4pi.presentation.engineModel;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.engineModel.RegisterEngineModelController;
import pt.isep.sem4pi.domain.model.manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;

import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("squid:S106")
public class RegisterEngineModelUI extends AbstractUI {
    private final RegisterEngineModelController controller = new RegisterEngineModelController();

    @Override
    protected boolean doShow() {
        final String modelCode = Console.readNonEmptyLine("Engine model code:", "Code is required.");
        final Manufacturer manufacturer = selectManufacturer();
        if (manufacturer == null) {
            return false;
        }
        final String name = Console.readNonEmptyLine("Engine model name:", "Name is required.");
        final EngineType type = selectEnum("Engine type:", EngineType.values());
        final BigDecimal power = readPositiveDecimal("Engine power:");
        final FuelType fuelType = selectEnum("Fuel type:", FuelType.values());
        final BigDecimal efficiency = readPositiveDecimal("Engine efficiency:");

        try {
            final EngineModel engineModel =
                    controller.registerEngineModel(modelCode, manufacturer.identity().toString(), name, type, power, fuelType, efficiency);
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

    private Manufacturer selectManufacturer() {
        final List<Manufacturer> manufacturers = controller.listManufacturers();
        if (manufacturers.isEmpty()) {
            System.out.println("No manufacturers available.");
            return null;
        }

        System.out.printf("%3s  %-10s  %-30s%n", "#", "Code", "Name");
        int idx = 1;
        for (final Manufacturer m : manufacturers) {
            System.out.printf("%3d) %-10s  %-30s%n", idx++, m.identity(), m.name());
        }

        final int choice = Console.readInteger("Select manufacturer by number (0 to cancel):");
        if (choice <= 0 || choice > manufacturers.size()) {
            System.out.println("Operation cancelled.");
            return null;
        }
        return manufacturers.get(choice - 1);
    }

    private <T extends Enum<T>> T selectEnum(final String label, final T[] values) {
        System.out.println(label);
        for (int i = 0; i < values.length; i++) {
            System.out.printf("%3d) %s%n", i + 1, values[i].name());
        }
        while (true) {
            final int choice = Console.readInteger("Select option:");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid option.");
        }
    }
}
