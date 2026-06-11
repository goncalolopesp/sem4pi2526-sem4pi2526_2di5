package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.TestFlightPlanController;
import pt.isep.sem4pi.domain.model.flight.DTO.TestFlightPlanResultDTO;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class TestFlightPlanUI extends AbstractUI {

    private final TestFlightPlanController controller = new TestFlightPlanController();

    @Override
    protected boolean doShow() {
        final List<FlightPlan> draftPlans = controller.listDraftFlightPlans();
        if (draftPlans.isEmpty()) {
            System.out.println("[INFO] No draft flight plans available for testing.");
            return false;
        }

        System.out.println("\nDraft Flight Plans:");
        for (int i = 0; i < draftPlans.size(); i++) {
            final FlightPlan plan = draftPlans.get(i);
            System.out.printf(
                    "%d. %s (flight %s, route %s, status %s)%n",
                    i + 1,
                    plan.identity().value(),
                    plan.flightId().value(),
                    plan.route().identity().code(),
                    plan.status()
            );
        }
        System.out.println("0. Cancel");

        final int choice = Console.readInteger("Select flight plan to test: ");
        if (choice == 0) {
            return false;
        }
        if (choice < 1 || choice > draftPlans.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final FlightPlan selected = draftPlans.get(choice - 1);

        try {
            final TestFlightPlanResultDTO result = controller.testFlightPlan(selected.identity().value());
            printResult(result);
        } catch (final Exception ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        return false;
    }

    private void printResult(final TestFlightPlanResultDTO result) {
        System.out.println("\n=== Flight Plan Test Result ===");
        System.out.println("Flight plan: " + result.flightPlanId());
        printPlanValidation(result.dslValidation());

        if (result.flightTest().executed()) {
            System.out.println("\n[C Flight Test]");
            System.out.println(result.flightTest().passed() ? "PASSED" : "FAILED");
            if (!result.flightTest().consoleOutput().isBlank()) {
                System.out.println(result.flightTest().consoleOutput());
            }
            if (!result.flightTest().reportPath().isBlank()) {
                System.out.println("Report: " + result.flightTest().reportPath());
            }
        } else {
            System.out.println("\n[C Flight Test] Skipped.");
            System.out.println(result.flightTest().consoleOutput());
        }

        System.out.println("\nFinal status: " + (result.accepted() ? "VALIDATED" : "REJECTED"));
        System.out.println(result.message());
    }

    private void printPlanValidation(final US120ValidationResult validation) {
        System.out.println("\n[Plan Validation]");
        if (validation.isValid()) {
            System.out.println("PASSED - stored flight plan data is consistent.");
            return;
        }

        final List<String> errors = new ArrayList<>(validation.getAllErrors());
        System.out.println("FAILED - " + errors.size() + " error(s):");
        for (final String error : errors) {
            System.out.println("  - " + error);
        }
    }

    @Override
    public String headline() {
        return "Test / Validate Flight Plan (US085)";
    }
}
