package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import pt.isep.sem4pi.application.controller.airtransportcompany.RegisterAirTransportCompanyController;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirTransportCompanyRepository;

import java.util.Scanner;

public class RegisterAirTransportCompanyUI {

    // We instantiate the controller and pass the persistent JPA repository.
    // In a real app, you might use a RepositoryFactory so you can easily swap between InMemory and JPA.
    private final RegisterAirTransportCompanyController controller = new RegisterAirTransportCompanyController(new JpaAirTransportCompanyRepository());
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        System.out.println("=== Register Air Transport Company ===");

        System.out.print("Enter Company Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter IATA Code (2 letters): ");
        String iata = scanner.nextLine();

        System.out.print("Enter ICAO Code (2-3 letters): ");
        String icao = scanner.nextLine();

        try {
            // Call the controller. If anything breaks our Domain rules, it throws an exception.
            controller.registerAirTransportCompany(name, iata, icao);
            System.out.println("\n[SUCCESS] Company registered successfully!");

        } catch (IllegalArgumentException | IllegalStateException e) {
            // We catch the domain validation errors (e.g., "IATA code must be exactly 2 letters")
            // and the service errors (e.g., "Company already exists")
            System.err.println("\n[ERROR] Failed to register company: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n[CRITICAL ERROR] An unexpected error occurred: " + e.getMessage());
        }
    }
}