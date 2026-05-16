package pt.isep.sem4pi.presentation.airtransportcompany;

import pt.isep.sem4pi.application.controller.airtransportcompany.RegisterAirTransportCompanyController;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirTransportCompanyRepository;

import java.util.Scanner;

public class RegisterAirTransportCompanyUI {

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
            controller.registerAirTransportCompany(name, iata, icao);
            System.out.println("\n[SUCCESS] Company registered successfully!");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("\n[ERROR] Failed to register company: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n[CRITICAL ERROR] An unexpected error occurred: " + e.getMessage());
        }
    }
}