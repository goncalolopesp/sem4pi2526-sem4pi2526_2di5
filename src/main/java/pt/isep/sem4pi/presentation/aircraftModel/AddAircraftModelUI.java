package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
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
        Iterable<Manufacturer> manufacturers = controller.getManufacturers();
        SelectWidget<Manufacturer> selectorMan = new SelectWidget<>("Manufacturers:", manufacturers);
        selectorMan.show();
        Manufacturer selectedMan = selectorMan.selectedElement();
        if (selectedMan == null) return false;

        /// 2. Simple data
        String name = Console.readLine("Model Name: ");

        List<String> validTypes = Arrays.asList("PASSENGER", "CARGO", "MIXED");

        SelectWidget<String> typeSelector = new SelectWidget<>("Select the Aircraft Type:", validTypes);
        typeSelector.show();

        String selectedType = typeSelector.selectedElement();

        if (selectedType == null) {
            System.out.println("Operation cancelled.");
            return false;
        }

        List<EngineType> validMotors = Arrays.asList(EngineType.values());

        SelectWidget<EngineType> motorSelector = new SelectWidget<>("Select the Motorization:", validMotors);
        motorSelector.show();

        EngineType motorization = motorSelector.selectedElement();

        if (motorization == null) {
            System.out.println("Operation cancelled.");
            return false;
        }

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
            SelectWidget<EngineModel> selectorEngine =
                    new SelectWidget<>("Available Compatible Engines (select one or 0 to stop):", compatibleEngines);
            selectorEngine.show();
            EngineModel selectedEngine = selectorEngine.selectedElement();

            if (selectedEngine == null) {
                addMore = false;
            } else {
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