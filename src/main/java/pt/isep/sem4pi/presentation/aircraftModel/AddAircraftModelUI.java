package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
import pt.isep.sem4pi.application.AddAircraftModelController;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.aircraftModel.MotorizationType;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User interface for adding a new aircraft model.
 * <p>This class extends AbstractUI and provides a console-based interface
 * for users to input details of a new aircraft model, including manufacturer,
 * basic data, weights, performance parameters, and associated engines.
 * It interacts with the AddAircraftModelController to persist the data.</p>
 */
public class AddAircraftModelUI extends AbstractUI {

    /**
     * The controller used to handle the business logic for adding aircraft models.
     */
    private final AddAircraftModelController controller = new AddAircraftModelController();

    /**
     * Displays the user interface for adding a new aircraft model.
     * <p>This method guides the user through selecting a manufacturer, entering
     * model details, inputting numerical parameters, and selecting engines.
     * It then attempts to create and save the aircraft model.</p>
     *
     * @return false to indicate the UI should not loop (single execution)
     */
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

        List<MotorizationType> validMotors = Arrays.asList(MotorizationType.values());

        SelectWidget<MotorizationType> motorSelector = new SelectWidget<>("Select the Motorization:", validMotors);
        motorSelector.show();

        MotorizationType motorization = motorSelector.selectedElement();

        if (motorization == null) {
            System.out.println("Operation cancelled.");
            return false;
        }

        /// 3. Collect BigDecimal values
        BigDecimal emptyWeight = readBigDecimal("Empty Weight (kg): ");
        BigDecimal mzfw = readBigDecimal("MZFW (Maximum Zero Fuel Weight, kg): ");
        BigDecimal mtow = readBigDecimal("MTOW (Maximum Take-Off Weight, kg): ");
        BigDecimal fuelCap = readBigDecimal("Fuel Capacity (liters): ");
        BigDecimal ceiling = readBigDecimal("Service Ceiling (feet): ");
        BigDecimal speed = readBigDecimal("Cruise Speed (knots): ");
        BigDecimal wingArea = readBigDecimal("Wing Area (m²): ");
        BigDecimal drag = readBigDecimal("Drag Coefficient: ");
        BigDecimal lift = readBigDecimal("Lift Coefficient: ");
        BigDecimal range = readBigDecimal("Maximum Range (km): ");
        BigDecimal aspectRatio = readBigDecimal("Aspect Ratio: ");

        /// 4. Choose Engines
        /// 4. Choose Engines
        List<EngineModel> selectedEngines = new ArrayList<>();
        Iterable<EngineModel> availableEngines = controller.getEngineModels();

        boolean addMore = true;
        while (addMore) {
            SelectWidget<EngineModel> selectorEngine =
                    new SelectWidget<>("Available Engines (select one or 0 to stop):", availableEngines);
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
            controller.addAircraftModel(selectedMan, selectedType, name, motorization,
                    emptyWeight, mzfw, mtow, fuelCap, ceiling, speed, wingArea, drag, lift, range, aspectRatio, selectedEngines);

            System.out.println("\nAircraft Model successfully created and saved!");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nUNEXPECTED ERROR: " + e.getMessage());
        }

        return false;
    }

    /**
     * Reads a BigDecimal value from the console with error handling.
     * <p>This method prompts the user for a decimal input and retries
     * until a valid number is entered.</p>
     *
     * @param prompt the prompt message to display to the user
     * @return the BigDecimal value entered by the user
     */
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
     * Returns the headline for this UI.
     *
     * @return the headline string
     */
    @Override
    public String headline() {
        return "Add New Aircraft Model";
    }
}