package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.CreateFlightPlanController;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.route.FlightRoute;

import java.util.ArrayList;
import java.util.List;

public class CreateFlightPlanUI extends AbstractUI {

    private final CreateFlightPlanController controller = new CreateFlightPlanController();

    @Override
    protected boolean doShow() {
        final List<FlightRoute> routes = new ArrayList<>();
        controller.getRoutes().forEach(routes::add);

        if (routes.isEmpty()) {
            System.out.println("[ERROR] No flight routes available. Create a route first.");
            return false;
        }

        System.out.println("\nFlight Routes:");
        for (int i = 0; i < routes.size(); i++) {
            final FlightRoute route = routes.get(i);
            System.out.printf(
                    "%d. %s (%s -> %s, company %s)%n",
                    i + 1,
                    route.identity().code(),
                    route.originAirport().code(),
                    route.destinationAirport().code(),
                    route.companyCode().code()
            );
        }
        System.out.println("0. Cancel");
        final int routeChoice = Console.readInteger("Select route: ");
        if (routeChoice == 0) {
            return false;
        }
        if (routeChoice < 1 || routeChoice > routes.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final FlightRoute selectedRoute = routes.get(routeChoice - 1);
        final String routeId = selectedRoute.identity().code();

        final List<Flight> flights = controller.getFlights();
        if (flights.isEmpty()) {
            System.out.println("[ERROR] No flights registered in the system.");
            return false;
        }

        System.out.println("\nFlights:");
        for (int i = 0; i < flights.size(); i++) {
            final Flight flight = flights.get(i);
            System.out.printf(
                    "%d. %s (aircraft: %s, status: %s)%n",
                    i + 1,
                    flight.identity().value(),
                    flight.aircraftRegistration().value(),
                    flight.status()
            );
        }
        System.out.println("0. Cancel");
        final int flightChoice = Console.readInteger("Select flight: ");
        if (flightChoice == 0) {
            return false;
        }
        if (flightChoice < 1 || flightChoice > flights.size()) {
            System.out.println("Invalid flight selection.");
            return false;
        }
        final Flight selectedFlight = flights.get(flightChoice - 1);
        final String flightId = selectedFlight.identity().value();
        final String aircraftRegistration = selectedFlight.aircraftRegistration().value();

        System.out.printf("%nAircraft assigned to flight: %s%n", aircraftRegistration);

        final String flightType = Console.readLine("Flight Type (REGULAR/CHARTER): ");

        final List<Pilot> pilots = controller.getPilotsForRoute(routeId);
        if (pilots.isEmpty()) {
            System.out.println("[ERROR] No pilots registered for this company.");
            return false;
        }

        System.out.println("\nPilots:");
        for (int i = 0; i < pilots.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, pilots.get(i).identity().code());
        }
        final int pilotChoice = Console.readInteger("Select pilot: ");
        if (pilotChoice < 1 || pilotChoice > pilots.size()) {
            System.out.println("Invalid pilot selection.");
            return false;
        }
        final String pilotId = pilots.get(pilotChoice - 1).identity().code();

        System.out.println("\n--- Leg 1 (route " + selectedRoute.originAirport().code()
                + " -> " + selectedRoute.destinationAirport().code() + ") ---");
        final int depDay = Console.readInteger("Departure day: ");
        final int depMonth = Console.readInteger("Departure month: ");
        final int depYear = Console.readInteger("Departure year: ");
        final int depHour = Console.readInteger("Departure hour: ");
        final int depMinute = Console.readInteger("Departure minute: ");
        final int depSecond = Console.readInteger("Departure second: ");
        final int arrHour = Console.readInteger("Arrival hour: ");
        final int arrMinute = Console.readInteger("Arrival minute: ");
        final int arrSecond = Console.readInteger("Arrival second: ");
        final double fuelQty = Console.readDouble("Fuel quantity: ");
        final String fuelUnit = Console.readLine("Fuel unit (KG/L): ");

        final CreateFlightPlanLegDTO leg = new CreateFlightPlanLegDTO(
                selectedRoute.originAirport().code(),
                depDay, depMonth, depYear,
                depHour, depMinute, depSecond,
                selectedRoute.destinationAirport().code(),
                arrHour, arrMinute, arrSecond,
                fuelQty,
                fuelUnit
        );

        final CreateFlightPlanDTO dto = new CreateFlightPlanDTO(
                null,
                routeId,
                flightId,
                flightType,
                null,
                pilotId,
                List.of(leg)
        );

        try {
            final var plan = controller.createFlightPlan(dto);
            System.out.printf(
                    "%nFlight plan %s created with status %s.%n",
                    plan.identity().value(),
                    plan.status()
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nUNEXPECTED ERROR: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Create Flight Plan";
    }
}
