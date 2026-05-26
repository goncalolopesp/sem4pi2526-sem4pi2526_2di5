package pt.isep.sem4pi.presentation.aircraft;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircraft.AddAircraftController;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.Scanner;

public class AddAircraftUI extends AbstractUI {

    private final AddAircraftController controller = new AddAircraftController(
            PersistenceContext.repositories().airTransportCompanies(),
            PersistenceContext.repositories().aircraftModels()
    );

    private final Scanner scanner = new Scanner(System.in);

    public boolean doShow() {
        System.out.println("=== Add Aircraft to Fleet ===");
        try {

            for (AirTransportCompany l : controller.getAirTransportCompanyList())
                System.out.println(l.identityIata().toString());

            System.out.print("Company IATA (e.g., TP): ");
            String iata = scanner.nextLine();
            System.out.print("Registration (e.g., CS-TPA): ");
            String reg = scanner.nextLine();

            System.out.print("Manufacturer (e.g., AIRBUS): ");
            String manufacturer = scanner.nextLine();
            System.out.print("Model Name (e.g., A320): ");
            String model = scanner.nextLine();

            System.out.print("Country: ");
            String country = scanner.nextLine();
            System.out.print("Economy Seats: ");
            int eco = Integer.parseInt(scanner.nextLine());
            System.out.print("Business Seats: ");
            int bus = Integer.parseInt(scanner.nextLine());
            System.out.print("First Class Seats: ");
            int first = Integer.parseInt(scanner.nextLine());

            controller.addAircraft(iata, reg, manufacturer, model, country, eco, bus, first);
            System.out.println("\n[SUCCESS] Aircraft added to fleet!");

            return true;
        } catch (Exception e) {
            System.err.println("\n[ERROR] " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Add Aircraft";
    }
}