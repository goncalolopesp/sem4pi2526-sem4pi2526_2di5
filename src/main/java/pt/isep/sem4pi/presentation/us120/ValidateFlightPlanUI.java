package pt.isep.sem4pi.presentation.us120;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.us120.US120FlightPlanValidator;

import java.io.IOException;
import java.nio.file.Path;

public class ValidateFlightPlanUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        final String filePath = Console.readNonEmptyLine(
                "Enter file path (.dsldig5):",
                "File path is required");

        try {
            US120FlightPlanValidator.validate(Path.of(filePath.trim()));
            return true;
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("\n[ERROR] Validation failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Validate Flight Plan";
    }
}
