package pt.isep.sem4pi.presentation.aircraft;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircraft.AddAircraftController;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

public class AddAircraftUI extends AbstractUI {

    private final AddAircraftController controller = new AddAircraftController(
            PersistenceContext.repositories().airTransportCompanies(),
            PersistenceContext.repositories().aircraftModels()
    );

    @Override
    protected boolean doShow() {
        try {
            /// 1. Escolher a Companhia Aérea (Air Transport Company)
            Iterable<AirTransportCompany> companiesIt = controller.getAirTransportCompanyList();
            List<AirTransportCompany> companies = new ArrayList<>();
            companiesIt.forEach(companies::add);

            if (companies.isEmpty()) {
                System.out.println("[ERROR] No Air Transport Companies available in the system.");
                return false;
            }

            System.out.println("Air Transport Companies:");
            for (int i = 0; i < companies.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, companies.get(i).identity().toString());
            }
            System.out.println("0. Exit");

            int compChoice = Console.readInteger("Select an option: ");
            if (compChoice == 0) return false;
            if (compChoice < 1 || compChoice > companies.size()) {
                System.out.println("[ERROR] Invalid option. Operation cancelled.");
                return false;
            }

            String selectedIata = companies.get(compChoice - 1).identity().toString();

            /// 2. Informação Básica
            String reg = Console.readLine("Registration (e.g., CS-TPA): ");
            String country = Console.readLine("Country of Registration: ");

            /// 3. Informação do Modelo
            String manufacturer = Console.readLine("Manufacturer (e.g., AIRBUS): ");
            String model = Console.readLine("Model Name (e.g., A320): ");

            /// 4. Configuração da Cabine
            System.out.println("\n--- Cabin Configuration ---");
            int eco = readPositiveInteger("Economy Seats: ");
            int bus = readPositiveInteger("Business Seats: ");
            int first = readPositiveInteger("First Class Seats: ");

            /// 5. Enviar para o Controller
            controller.addAircraft(selectedIata, reg, manufacturer, model, country, eco, bus, first);
            System.out.println("\n[SUCCESS] Aircraft " + reg + " successfully added to the fleet!");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[UNEXPECTED ERROR] " + e.getMessage());
        }

        return false;
    }

    private int readPositiveInteger(String prompt) {
        while (true) {
            int val = Console.readInteger(prompt);
            if (val >= 0) return val;
            System.out.println("[ERROR] Number of seats cannot be negative.");
        }
    }

    @Override
    public String headline() {
        return "Add Aircraft to Fleet";
    }
}