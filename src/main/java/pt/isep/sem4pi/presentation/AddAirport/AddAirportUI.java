package pt.isep.sem4pi.presentation.AddAirport;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.addairport.AddAirportController;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirportRepository;

import java.util.Scanner;

public class AddAirportUI extends AbstractUI {

    private final AddAirportController controller = new AddAirportController(new JpaAirportRepository());
    private final Scanner scanner = new Scanner(System.in);

    public boolean doShow() {
        System.out.println("=== Register an Airport ===");

        try {
            System.out.print("Enter Airport Name: ");
            String name = scanner.nextLine();

            System.out.print("Enter Town: ");
            String town = scanner.nextLine();

            System.out.print("Enter Country: ");
            String country = scanner.nextLine();

            System.out.print("Enter IATA Code (3 letters): ");
            String iata = scanner.nextLine();

            System.out.print("Enter ICAO Code (4 letters): ");
            String icao = scanner.nextLine();

            System.out.print("Enter Latitude (-90 to 90): ");
            double lat = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter Longitude (-180 to 180): ");
            double lon = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter Elevation (meters above sea level): ");
            double elevation = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter Air Control Area ID (e.g., EUR_WEST): ");
            String areaId = scanner.nextLine();

            controller.addAirport(name, town, country, iata, icao, lat, lon, elevation, areaId);
            System.out.println("\n[SUCCESS] Airport registered successfully!");

            return true;
        } catch (NumberFormatException e) {
            System.err.println("\n[ERROR] Coordinates and Elevation must be valid numbers.");
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("\n[ERROR] Failed to register airport: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("\n[CRITICAL ERROR] An unexpected error occurred: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Add Airport";
    }
}