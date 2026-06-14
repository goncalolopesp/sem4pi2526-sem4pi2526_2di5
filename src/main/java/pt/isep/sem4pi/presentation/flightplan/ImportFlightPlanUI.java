package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.ImportFlightPlanController;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;

public class ImportFlightPlanUI extends AbstractUI {

    private final ImportFlightPlanController controller = new ImportFlightPlanController();

    @Override
    protected boolean doShow() {
        final String filePath = Console.readNonEmptyLine(
                "Enter flight plan file path (.dsldig5):",
                "File path is required");

        try {
            final FlightPlanImportResultDTO result = controller.importFlightPlan(filePath.trim());
            printResult(result);
            return result.isSuccess();
        } catch (final IllegalArgumentException e) {
            System.err.println("\n[ERROR] Import failed: " + e.getMessage());
            return false;
        }
    }

    private void printResult(final FlightPlanImportResultDTO result) {
        if (result.isSuccess()) {
            System.out.println("\n[SUCCESS] Flight plan imported successfully.");
            System.out.println("Flight plan id: " + result.getFlightPlanId());
            return;
        }

        System.err.println("\n[ERROR] Flight plan import rejected. The file is invalid or cannot be used by the system.");
        for (final String error : result.getErrors()) {
            System.err.println(" - " + error);
        }
    }

    @Override
    public String headline() {
        return "Import Flight Plan From File";
    }
}
