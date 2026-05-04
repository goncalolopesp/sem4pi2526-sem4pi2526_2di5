//package pt.isep.sem4pi.presentation.authz;
//
//import eapli.framework.io.util.Console;
//import eapli.framework.presentation.console.AbstractUI;
//import eapli.framework.presentation.console.SelectWidget;
//import pt.isep.sem4pi.application.AddAircraftModelController;
//import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
//import pt.isep.sem4pi.domain.model.Manufacturer.Manufacturer;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//public class AddAircraftModelUI extends AbstractUI {
//
//    private final AddAircraftModelController theController = new AddAircraftModelController();
//
//    @Override
//    protected boolean doShow() {
//        ///  Widgets
//        final Iterable<Manufacturer> manufacturers = this.theController.getManufacturers();
//        final SelectWidget<Manufacturer> manufacturerSelector = new SelectWidget<>("Select Manufacturer:", manufacturers);
//        manufacturerSelector.show();
//        final Manufacturer selectedManufacturer = manufacturerSelector.selectedElement();
//
//        /// If User chooses 0 (Exit), widget returns null
//        if (selectedManufacturer == null) {
//            System.out.println("Operation cancelled.");
//            return false;
//        }
//
//        /// Read data with console
//        final String codeId = Console.readLine("Aircraft Model ID / Code:");
//        final String name = Console.readLine("Model Name:");
//        final String type = Console.readLine("Type (PASSENGER, CARGO, MIXED):");
//        final String motorization = Console.readLine("Motorization Description:");
//
//        // Dica: Para BigDecimal, lês como Double e convertes a seguir
//        final BigDecimal emptyWeight = BigDecimal.valueOf(Console.readDouble("Empty Weight:"));
//        // ... (repetir para os restantes pesos e coeficientes) ...
//        final BigDecimal maximumTakeoffWeight = BigDecimal.valueOf(Console.readDouble("Maximum Takeoff Weight:"));
//
//        // 4. Selecionar Motores (Ciclo para garantir que adiciona 1 ou mais)
//        List<EngineModel> selectedEngines = new ArrayList<>();
//        boolean keepAdding = true;
//        final Iterable<EngineModel> engineList = this.theController.getEngineModels();
//
//        while (keepAdding) {
//            SelectWidget<EngineModel> engineSelector = new SelectWidget<>("Select an Engine to add:", engineList);
//            engineSelector.show();
//            EngineModel engine = engineSelector.selectedElement();
//
//            if (engine != null) {
//                selectedEngines.add(engine);
//                String answer = Console.readLine("Add another engine? (Y/N): ");
//                keepAdding = answer.equalsIgnoreCase("Y");
//            } else {
//                keepAdding = false;
//            }
//        }
//
//        // 5. Chamar o Controlador e tratar erros com try-catch
//        try {
//            // Nota: Passar os dados recolhidos para o método do controller (tens de os colocar todos na assinatura)
//            this.theController.addAircraftModel(codeId, selectedManufacturer, type, name, motorization, emptyWeight, /*... outros BigDecimal ...*/ selectedEngines);
//            System.out.println("Aircraft Model successfully created!");
//            return true;
//        } catch (Exception e) {
//            // Apanha erros de Invariantes (ex: peso negativo) e mostra ao operador
//            System.out.println("Error creating Aircraft Model: " + e.getMessage());
//            return false;
//        }
//    }
//
//    @Override
//    public String headline() {
//        return "Add New Aircraft Model";
//    }
//}