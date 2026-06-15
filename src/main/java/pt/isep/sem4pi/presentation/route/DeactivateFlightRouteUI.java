package pt.isep.sem4pi.presentation.route;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.route.DeactivateFlightRouteController;
import pt.isep.sem4pi.domain.model.route.FlightRoute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * User interface for deactivating (deleting) a flight route from a given date onwards.
 * <p>
 * US074 - As an Air Transport Company Collaborator, I want to deactivate a flight route
 * from a given date onwards.
 */

public class DeactivateFlightRouteUI extends AbstractUI {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DeactivateFlightRouteController controller = new DeactivateFlightRouteController();

    @Override
    protected boolean doShow() {
        // 1. List all active flight routes
        final Iterable<FlightRoute> allRoutes = controller.getRoutes();
        final List<FlightRoute> activeRoutes = new ArrayList<>();
        for (final FlightRoute route : allRoutes) {
            if (route.isActive()) {
                activeRoutes.add(route);
            }
        }

        if (activeRoutes.isEmpty()) {
            System.out.println("No active flight routes available to deactivate.");
            return false;
        }

        System.out.printf("%3s  %-15s%n", "#", "Route ID");
        System.out.println("---------------------");
        int idx = 1;
        for (final FlightRoute route : activeRoutes) {
            System.out.printf("%3d) %-15s%n", idx++, route.identity().toString());
        }
        System.out.println("  0) Cancel");

        // 2. Select a route
        final int choice = Console.readInteger("Select flight route by number (0 to cancel): ");
        if (choice <= 0 || choice > activeRoutes.size()) {
            System.out.println("Operation cancelled.");
            return false;
        }

        final FlightRoute selectedRoute = activeRoutes.get(choice - 1);

        // 3. Read deactivation date
        final LocalDate deactivationDate = readDeactivationDate(selectedRoute.identity().toString());
        if (deactivationDate == null) {
            System.out.println("Operation cancelled.");
            return false;
        }

        // 4. Confirm operation
        System.out.printf("%nYou are about to deactivate route '%s' from %s onwards.%n",
                selectedRoute.identity().toString(), deactivationDate.format(DATE_FORMAT));
        final boolean confirm = Console.readBoolean("Are you sure? (y/n): ");
        if (!confirm) {
            System.out.println("Operation cancelled.");
            return false;
        }

        // 5. Execute deactivation
        try {
            controller.deactivateRoute(selectedRoute.identity().toString(), deactivationDate);
            System.out.printf("%nFlight Route '%s' successfully deactivated from %s onwards!%n",
                    selectedRoute.identity().toString(), deactivationDate.format(DATE_FORMAT));
        } catch (final IllegalArgumentException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (final IllegalStateException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("\nUNEXPECTED ERROR: " + e.getMessage());
        }

        return false;
    }

    /**
     * Reads and validates a deactivation date from the user.
     *
     * @return the parsed LocalDate, or null if the user types 'cancel'
     */
    private LocalDate readDeactivationDate(String routeId) {
        while (true) {
            final String input = Console.readLine("Deactivation date (yyyy-MM-dd) or 'cancel' to abort: ");
            if ("cancel".equalsIgnoreCase(input.trim())) {
                return null;
            }
            try {
                final LocalDate date = LocalDate.parse(input.trim(), DATE_FORMAT);
                if (date.isBefore(LocalDate.now())) {
                    System.out.println("[ERROR] Deactivation date cannot be in the past.");
                    continue;
                }

                controller.validateDeactivation(routeId, date);

                return date;
            } catch (final DateTimeParseException e) {
                System.out.println("[ERROR] Invalid date format. Please use yyyy-MM-dd (e.g. 2026-06-15).");
            } catch (final IllegalStateException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    @Override
    public String headline() {
        return "Deactivate Flight Route";
    }
}
