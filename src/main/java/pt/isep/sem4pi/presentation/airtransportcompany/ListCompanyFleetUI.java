package pt.isep.sem4pi.presentation.airtransportcompany;

import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.io.util.Console;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCompanyFleetController;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;



public class ListCompanyFleetUI extends AbstractUI {

    private final ListCompanyFleetController controller = new ListCompanyFleetController();

    @Override
    protected boolean doShow() {
        // 1. Apresentar as opções das alíneas a, b, c, d (sem linhas extra)
        ListCompanyFleetController.FleetSortOption[] options = ListCompanyFleetController.FleetSortOption.values();
        System.out.println("How do you want to list the fleet?");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s\n", i + 1, options[i].name());
        }
        System.out.println("0. Exit");

        int choice = Console.readInteger("Select an option: ");

        if (choice == 0) {
            System.out.println("Operation cancelled.");
            return false;
        }

        if (choice < 1 || choice > options.length) {
            System.out.println("Invalid option. Operation cancelled.");
            return false;
        }

        ListCompanyFleetController.FleetSortOption selectedOption = options[choice - 1];

        System.out.println("\nRetrieving your company's fleet (Sorted by " + selectedOption.name() + ")...\n");

        try {
            // 2. Chamar o controller com a opção escolhida
            Iterable<Aircraft> fleet = controller.listMyCompanyFleet(selectedOption);

            if (!fleet.iterator().hasNext()) {
                System.out.println("There are no aircraft registered in your company's fleet.");
                return false;
            }

            // 3. Imprimir a tabela formatada com a nova coluna "Manufacturer"
            System.out.printf("%-15s | %-15s | %-15s | %-10s | %-6s | %-15s\n", "Registration", "Manufacturer", "Aircraft Model", "Capacity", "Age", "Status");
            System.out.println("-----------------------------------------------------------------------------------------");

            for (Aircraft aircraft : fleet) {
                System.out.printf("%-15s | %-15s | %-15s | %-10d | %-6d | %-15s\n",
                        aircraft.identity().toString(),
                        aircraft.modelId().manufacturerId().toString(), // Imprime o Maker
                        aircraft.modelId().modelName(),
                        aircraft.seats().getTotalCapacity(),
                        aircraft.age(),
                        aircraft.status().toString());
            }

            System.out.println("-----------------------------------------------------------------------------------------");

            System.out.println("-------------------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "List My Company Fleet";
    }
}