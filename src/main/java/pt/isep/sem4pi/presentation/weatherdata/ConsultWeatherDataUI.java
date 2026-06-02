package pt.isep.sem4pi.presentation.weatherdata;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.weatherdata.ConsultWeatherDataController;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsultWeatherDataUI extends AbstractUI {
    private final ConsultWeatherDataController controller = new ConsultWeatherDataController();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    protected boolean doShow() {
        System.out.println("=== Consult Weather Data ===");
        System.out.print("Enter Air Control Area ID: ");
        String areaId = scanner.nextLine();
        System.out.print("Enter Date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();

        try {
            LocalDate date = LocalDate.parse(dateInput.trim());
            List<WeatherDataViewDTO> results = controller.consultByAreaAndDate(areaId, date);
            if (results.isEmpty()) {
                System.out.println("\nNo weather data found for " + date + " in area " + areaId + ".");
                return false;
            }
            printResults(results);
            return true;
        } catch (DateTimeParseException e) {
            System.err.println("\n[ERROR] Invalid date format. Use YYYY-MM-DD.");
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("\n[ERROR] " + e.getMessage());
            return false;
        }
    }

    private void printResults(List<WeatherDataViewDTO> results) {
        System.out.println("\nWeather data results:");
        System.out.printf("%-8s %-8s %-10s %-16s %-14s %-14s %-14s%n",
                "Start", "End", "Temp(C)", "Wind(kts/deg)", "Pressure(hPa)", "Humidity(%)", "Altitude(m)");
        for (WeatherDataViewDTO dto : results) {
            String wind = String.format("%.1f/%.0f", dto.getWindSpeedKnots(), dto.getWindDirectionDegrees());
            System.out.printf("%-8s %-8s %-10.1f %-16s %-14.1f %-14.1f %-14.1f%n",
                    dto.getStartTime(),
                    dto.getEndTime(),
                    dto.getTemperatureCelsius(),
                    wind,
                    dto.getPressureHpa(),
                    dto.getHumidityPercentage(),
                    dto.getAltitudeMeters());
        }
    }

    @Override
    public String headline() {
        return "Consult Weather Data";
    }
}
