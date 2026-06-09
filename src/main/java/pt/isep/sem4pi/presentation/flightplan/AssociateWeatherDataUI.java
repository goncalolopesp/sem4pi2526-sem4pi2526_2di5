package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.AssociateWeatherDataController;
import pt.isep.sem4pi.application.service.flightplan.WeatherAssociationResult;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.weatherdata.DTO.WeatherDataViewDTO;

import java.util.List;
import java.util.Scanner;

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
            System.out.print("Weather row number: ");
            final int row = Integer.parseInt(scanner.nextLine().trim());
            if (row < 1 || row > weather.size()) {
                throw new IllegalArgumentException("Invalid weather row number.");
            }

            final WeatherDataViewDTO selected = weather.get(row - 1);
            final WeatherAssociationResult result = controller.associate(
                    planId, selected.getAirControlAreaId(), selected.getDate(),
                    selected.getStartTime(), selected.getEndTime());

            System.out.println("Weather data associated with flight plan "
                    + result.flightPlan().identity().value() + ".");
            if (result.previousTestInvalidated()) {
                System.out.println("[WARNING] The previous flight plan test is no longer valid.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("[ERROR] Weather row number must be numeric.");
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
        System.out.printf("%n%-4s %-10s %-10s %-8s %-8s%n",
                "Row", "Area", "Date", "Start", "End");
        for (int i = 0; i < weather.size(); i++) {
            final WeatherDataViewDTO data = weather.get(i);
            System.out.printf("%-4d %-10s %-10s %-8s %-8s%n", i + 1,
                    data.getAirControlAreaId(), data.getDate(),
                    data.getStartTime(), data.getEndTime());
        }
    }

    @Override
    public String headline() {
        return "Add Weather Data to Flight Plan";
    }
}
