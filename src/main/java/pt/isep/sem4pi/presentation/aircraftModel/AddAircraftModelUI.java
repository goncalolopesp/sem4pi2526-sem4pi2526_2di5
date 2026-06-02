package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircraftModel.AddAircraftModelController;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.AircraftModelDTO;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User interface for adding a new aircraft model.
 */
public class AddAircraftModelUI extends AbstractUI {

    private final AddAircraftModelController controller = new AddAircraftModelController();

    @Override
    protected boolean doShow() {
        /// 1. Choose Manufacturer
        // 1. Choose Manufacturer (manual menu to avoid extra blank lines)
        Iterable<Manufacturer> manufacturersIt = controller.getManufacturers();
        List<Manufacturer> manufacturers = new ArrayList<>();
        manufacturersIt.forEach(manufacturers::add);
        if (manufacturers.isEmpty()) {
            System.out.println("[ERROR] No manufacturers available.");
            return false;
        }

        System.out.println("Manufacturers:");
        for (int i = 0; i < manufacturers.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, manufacturers.get(i).toString());
        }
        System.out.println("0. Exit");
        int manChoice = Console.readInteger("Select an option: ");
        if (manChoice == 0) return false;
        if (manChoice < 1 || manChoice > manufacturers.size()) {
            System.out.println("Invalid option. Operation cancelled.");
            return false;
        }
        Manufacturer selectedMan = manufacturers.get(manChoice - 1);

        /// 2. Simple data
        String name = Console.readLine("Model Name: ");

        List<String> validTypes = Arrays.asList("PASSENGER", "CARGO", "MIXED");

        // 2. Select aircraft type
        System.out.println("Select the Aircraft Type:");
        for (int i = 0; i < validTypes.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, validTypes.get(i));
        }
        System.out.println("0. Exit");
        int typeChoice = Console.readInteger("Select an option: ");
        if (typeChoice == 0) return false;
        if (typeChoice < 1 || typeChoice > validTypes.size()) {
            System.out.println("Invalid option. Operation cancelled.");
            return false;
        }
        String selectedType = validTypes.get(typeChoice - 1);

        

        // 3. Select motorization
        List<EngineType> motorList = Arrays.asList(EngineType.values());
        System.out.println("Select the Motorization:");
        for (int i = 0; i < motorList.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, motorList.get(i).name());
        }
        System.out.println("0. Exit");
        int motorChoice = Console.readInteger("Select an option: ");
        if (motorChoice == 0) return false;
        if (motorChoice < 1 || motorChoice > motorList.size()) {
            System.out.println("Invalid option. Operation cancelled.");
            return false;
        }
        EngineType motorization = motorList.get(motorChoice - 1);

        Iterable<EngineModel> allEngines = controller.getEngineModels();
        List<EngineModel> compatibleEngines = new ArrayList<>();

        for (EngineModel engine : allEngines) {
            if (engine.type() == motorization) {
                compatibleEngines.add(engine);
            }
        }

        if (compatibleEngines.isEmpty()) {
            System.out.println("\n[ERROR] There are no " + motorization.name() + " engines registered in the system.");
            System.out.println("Operation cancelled. Please register a compatible engine first.");
            return false;
        }

        /// 3. Collect BigDecimal values and Capacity with Domain Validations
        BigDecimal emptyWeight = readBigDecimal("Empty Weight (kg): ");
        BigDecimal mzfw = readMzfw(emptyWeight);
        BigDecimal mtow = readMtow(mzfw);
        BigDecimal fuelCap = readBigDecimal("Fuel Capacity (liters): ");
        BigDecimal ceiling = readBigDecimal("Service Ceiling (feet): ");
        BigDecimal speed = readBigDecimal("Cruise Speed (knots): ");
        BigDecimal wingArea = readBigDecimal("Wing Area (m²): ");
        BigDecimal drag = readDrag();
        BigDecimal lift = readLift();
        BigDecimal range = readBigDecimal("Maximum Range (km): ");
        BigDecimal aspectRatio = readBigDecimal("Aspect Ratio: ");

        Integer maximumCapacity = Console.readInteger("Maximum Capacity (number of seats): ");

        /// 4. Choose Engines
        List<EngineModel> selectedEngines = new ArrayList<>();

        boolean addMore = true;
        while (addMore) {
            System.out.println("Available Compatible Engines (select one or 0 to stop):");
            for (int i = 0; i < compatibleEngines.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, compatibleEngines.get(i).toString());
            }
            System.out.println("0. Stop");
            int engChoice = Console.readInteger("Select an option: ");

            if (engChoice == 0) {
                addMore = false;
            } else if (engChoice < 0 || engChoice > compatibleEngines.size()) {
                System.out.println("Invalid option. Stopping selection.");
                addMore = false;
            } else {
                EngineModel selectedEngine = compatibleEngines.get(engChoice - 1);
                boolean alreadySelected = selectedEngines.stream()
                        .anyMatch(e -> e.identity().equals(selectedEngine.identity()));

                if (!alreadySelected) {
                    selectedEngines.add(selectedEngine);
                    System.out.println("Engine added: " + selectedEngine);
                } else {
                    System.out.println("Engine already selected.");
                }
                addMore = Console.readBoolean("Add another engine? (y/n): ");
            }
        }

        if (selectedEngines.isEmpty()) {
            System.out.println("At least one engine must be selected.");
            return false;
        }

        /// 5. Send to the Controller
        try {
            AircraftModelDTO dto = controller.addAircraftModel(selectedMan, selectedType, name, motorization,
                    emptyWeight, mzfw, mtow, fuelCap, ceiling, speed, wingArea, drag, lift, range, aspectRatio, selectedEngines, maximumCapacity);

            System.out.println("\nAircraft Model " + dto.getModelName() + " successfully created and saved (Version: " + dto.getVersion() + ")!");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nUNEXPECTED ERROR: " + e.getMessage());
        }

        return false;
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            try {
                double value = Console.readDouble(prompt);
                return BigDecimal.valueOf(value);
            } catch (Exception e) {
                System.out.println("Invalid number. Please enter a valid decimal value.");
            }
        }
    }

    /**
     * Helper methods to enforce domain rules immediately during UI input.
     */
    private BigDecimal readMzfw(BigDecimal emptyWeight) {
        while (true) {
            BigDecimal mzfw = readBigDecimal("MZFW (Maximum Zero Fuel Weight, kg): ");
            if (mzfw.compareTo(emptyWeight) > 0) return mzfw;
            System.out.println("[ERROR] MZFW must be strictly greater than Empty Weight (" + emptyWeight + ").");
        }
    }

    private BigDecimal readMtow(BigDecimal mzfw) {
        while (true) {
            BigDecimal mtow = readBigDecimal("MTOW (Maximum Take-Off Weight, kg): ");
            if (mtow.compareTo(mzfw) > 0) return mtow;
            System.out.println("[ERROR] MTOW must be strictly greater than MZFW (" + mzfw + ").");
        }
    }

    private BigDecimal readDrag() {
        while (true) {
            BigDecimal drag = readBigDecimal("Drag Coefficient: ");
            if (drag.compareTo(BigDecimal.ZERO) > 0 && drag.compareTo(new BigDecimal("1.0")) <= 0) return drag;
            System.out.println("[ERROR] Drag Coefficient must be strictly > 0 and <= 1.0.");
        }
    }

    private BigDecimal readLift() {
        while (true) {
            BigDecimal lift = readBigDecimal("Lift Coefficient: ");
            if (lift.compareTo(BigDecimal.ZERO) > 0 && lift.compareTo(new BigDecimal("4.0")) <= 0) return lift;
            System.out.println("[ERROR] Lift Coefficient must be strictly > 0 and <= 4.0.");
        }
    }

    @Override
    public String headline() {
        return "Add New Aircraft Model";
    }
}