package pt.isep.sem4pi.presentation.weatherdata;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.weatherdata.RegisterWeatherController;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaWeatherBatchRepository;

import java.util.Scanner;

public class RegisterWeatherUI extends AbstractUI {
    private final RegisterWeatherController controller = new RegisterWeatherController(new JpaWeatherBatchRepository());
    private final Scanner scanner = new Scanner(System.in);

    public boolean doShow() {
        System.out.println("=== Register Manual Weather Data ===");
        try {
            System.out.print("Enter Air Control Area ID: ");
            String areaId = scanner.nextLine();
            System.out.print("Enter Temperature (Celsius): ");
            double temp = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Wind Speed (Knots): ");
            double windSpeed = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Wind Direction (0-359): ");
            double windDir = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Humidity (%): ");
            double humidity = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Pressure (hPa): ");
            double pressure = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Altitude (meters): ");
            double altitude = Double.parseDouble(scanner.nextLine());

            controller.registerWeather(areaId, temp, windSpeed, windDir, humidity, pressure, altitude);
            System.out.println("\n[SUCCESS] Weather data registered successfully!");

            return true;
        } catch (NumberFormatException e) {
            System.err.println("\n[ERROR] All metrics must be valid numbers.");
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("\n[ERROR] Failed to register data: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Register Weather Data";
    }
}