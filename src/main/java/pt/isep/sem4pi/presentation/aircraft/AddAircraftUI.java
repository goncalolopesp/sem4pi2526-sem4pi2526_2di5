package pt.isep.sem4pi.presentation.aircraft;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircraft.AddAircraftController;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftModelTempRepository;

import java.util.Scanner;

public class AddAircraftUI extends AbstractUI {

    private final AddAircraftController controller = new AddAircraftController(
            new JpaAirTransportCompanyRepository(),
            new InMemoryAircraftModelTempRepository()
    );

    private final Scanner scanner = new Scanner(System.in);

    @Override
    protected boolean doShow() {
        System.out.println("=== Add Aircraft to Fleet ===");
        try {

            System.out.println("\nAvailable companies:");
            for (AirTransportCompany l : controller.getAirTransportCompanyList()) {
                System.out.println(" - " + l.identityIata().toString());
            }

            System.out.print("\nCompany IATA (e.g., TP): ");
            final String iata = scanner.nextLine().trim();

            System.out.print("Registration (e.g., CS-TPA): ");
            final String reg = scanner.nextLine().trim();

            System.out.print("Manufacturer code (e.g., AIRBUS, BOEING): ");
            final String manufacturerCode = scanner.nextLine().trim().toUpperCase();

            System.out.print("Model name (e.g., A320, B737): ");
            final String modelName = scanner.nextLine().trim();

            System.out.print("Country: ");
            final String country = scanner.nextLine().trim();

            System.out.print("Economy Seats: ");
            final int eco = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Business Seats: ");
            final int bus = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("First Class Seats: ");
            final int first = Integer.parseInt(scanner.nextLine().trim());

            controller.addAircraft(iata, reg, manufacturerCode, modelName, country, eco, bus, first);
            System.out.println("\n[SUCCESS] Aircraft added to fleet!");

            return true;

        } catch (NumberFormatException e) {
            System.err.println("\n[ERROR] Invalid number for seats.");
            return false;
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