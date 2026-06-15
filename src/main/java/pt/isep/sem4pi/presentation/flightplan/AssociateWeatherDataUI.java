package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.AssociateWeatherDataController;
import pt.isep.sem4pi.application.service.flightplan.WeatherAssociationResult;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Console interface for US082.
 *
 * <p>It presents accessible plans and complete weather measurements, then
 * converts comma-separated row numbers into persisted weather identifiers.
 * A {@link LinkedHashSet} removes repeated selections while preserving the
 * order chosen by the user.</p>
 */
public class AssociateWeatherDataUI extends AbstractUI {

    private final AssociateWeatherDataController controller = new AssociateWeatherDataController();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    protected boolean doShow() {
        try {
            final List<FlightPlan> plans = controller.myFlightPlans();
            if (plans.isEmpty()) {
                System.out.println("You have no flight plans.");
                return false;
            }
            printPlans(plans);
            System.out.print("Flight plan ID: ");
            final String planId = scanner.nextLine().trim();

            final List<WeatherDataViewDTO> weather = controller.weatherData();
            if (weather.isEmpty()) {
                System.out.println("No weather data exists in the system.");
                return false;
            }
            printWeather(weather);
            System.out.print("Weather rows (comma-separated, e.g. 1,3,5): ");
            final List<Long> selectedIds = selectedWeatherIds(scanner.nextLine(), weather);
            final WeatherAssociationResult result = controller.associate(planId, selectedIds);

            System.out.printf("%d weather record(s) associated with flight plan %s.%n",
                    result.associatedWeatherCount(), result.flightPlan().identity().value());
            if (result.associatedWeatherCount() == 0) {
                System.out.println("All selected weather records were already associated.");
            }
            if (result.previousTestInvalidated()) {
                System.out.println("[WARNING] The previous flight plan test is no longer valid.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("[ERROR] Weather rows must be comma-separated numbers.");
        } catch (Exception ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
        return false;
    }

    private void printPlans(List<FlightPlan> plans) {
        System.out.printf("%n%-12s %-10s %-10s%n", "Plan ID", "Flight", "Status");
        plans.forEach(plan -> System.out.printf("%-12s %-10s %-10s%n",
                plan.identity().value(), plan.flightId().value(), plan.status()));
    }

    private void printWeather(List<WeatherDataViewDTO> weather) {
        System.out.printf("%n%-4s %-8s %-10s %-5s %-5s %8s %9s %9s %10s %9s %10s%n",
                "Row", "Area", "Date", "From", "To", "Temp C", "Wind kt",
                "Wind deg", "Press hPa", "Hum %", "Alt m");
        for (int i = 0; i < weather.size(); i++) {
            final WeatherDataViewDTO data = weather.get(i);
            System.out.printf("%-4d %-8s %-10s %-5s %-5s %8.1f %9.1f %9.1f %10.1f %9.1f %10.1f%n",
                    i + 1,
                    data.getAirControlAreaId(), data.getDate(),
                    data.getStartTime(), data.getEndTime(),
                    data.getTemperatureCelsius(), data.getWindSpeedKnots(),
                    data.getWindDirectionDegrees(), data.getPressureHpa(),
                    data.getHumidityPercentage(), data.getAltitudeMeters());
        }
    }

    private List<Long> selectedWeatherIds(String input, List<WeatherDataViewDTO> weather) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Select at least one weather row.");
        }
        final Set<Long> ids = new LinkedHashSet<>();
        for (String token : input.split(",")) {
            final int row = Integer.parseInt(token.trim());
            if (row < 1 || row > weather.size()) {
                throw new IllegalArgumentException("Invalid weather row number: " + row);
            }
            final Long id = weather.get(row - 1).getWeatherDataId();
            if (id == null) {
                throw new IllegalStateException("Selected weather data has no persisted ID.");
            }
            ids.add(id);
        }
        return List.copyOf(ids);
    }

    @Override
    public String headline() {
        return "Add Weather Data to Flight Plan";
    }
}
