package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.flightplan.CreateFlightPlanFromFileController;
import pt.isep.sem4pi.application.service.flightplan.FlightPlanFileFormat;
import pt.isep.sem4pi.domain.model.flight.DTO.FlightPlanImportResultDTO;

public class CreateFlightPlanFromFileUI extends AbstractUI {

    private final CreateFlightPlanFromFileController controller = new CreateFlightPlanFromFileController();

    @Override
    protected boolean doShow() {
        System.out.println();
        System.out.println("Supported formats: " + FlightPlanFileFormat.supportedFormatsMessage());

        final String filePath = Console.readNonEmptyLine(
                "Enter flight plan file path:",
                "File path is required");

        try {
            final FlightPlanImportResultDTO result = controller.createFromFile(filePath.trim());
            if (result.isSuccess()) {
                System.out.println("\n[SUCCESS] Flight plan created from file.");
                System.out.println("Flight plan id: " + result.getFlightPlanId());
                return true;
            }

            printRejected(result);
            return false;
        } catch (final IllegalArgumentException e) {
            System.err.println("\n[ERROR] " + e.getMessage());
            return false;
        }
    }

    private void printRejected(final FlightPlanImportResultDTO result) {
        System.err.println("\n[ERROR] Flight plan file rejected. It cannot be imported or used by the system.");
        for (final String error : result.getErrors()) {
            System.err.println(" - " + error);
        }
    }

    @Override
    public String headline() {
        return "Create Flight Plan From File (US121)";
    }
}
