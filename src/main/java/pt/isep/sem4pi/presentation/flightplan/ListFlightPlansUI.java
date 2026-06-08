package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.CreateFlightPlanController;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;

import java.util.List;

public class ListFlightPlansUI extends AbstractUI {

    private final CreateFlightPlanController controller = new CreateFlightPlanController();

    @Override
    protected boolean doShow() {
        try {
            final List<FlightPlan> plans = controller.listFlightPlans();

            if (plans.isEmpty()) {
                System.out.println("\nNo flight plans registered.");
                return false;
            }

            System.out.printf(
                    "%n%-12s | %-10s | %-8s | %-12s | %-10s | %-10s | %-5s%n",
                    "Plan ID", "Flight", "Route", "Aircraft", "Pilot", "Type", "Status"
            );
            System.out.println("--------------------------------------------------------------------------------");

            for (final FlightPlan plan : plans) {
                System.out.printf(
                        "%-12s | %-10s | %-8s | %-12s | %-10s | %-10s | %-5s%n",
                        plan.identity().value(),
                        plan.flightId().value(),
                        plan.route().identity().code(),
                        plan.aircraft().identity().value(),
                        plan.pilot().identity().code(),
                        plan.flightType(),
                        plan.status()
                );
            }

            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("Total: %d flight plan(s)%n", plans.size());

        } catch (Exception e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Flight Plans";
    }
}
