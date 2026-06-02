package pt.isep.sem4pi.presentation.aircraft;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.aircraft.RetireAircraftController;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("squid:S106")
public class RetireAircraftUI extends AbstractUI {

    private final RetireAircraftController controller = new RetireAircraftController();

    @Override
    protected boolean doShow() {
        final List<Aircraft> activeAircraft = controller.listAircraft()
                .stream()
                .filter(aircraft -> !aircraft.isRetired())
                .collect(Collectors.toList());

        if (activeAircraft.isEmpty()) {
            System.out.println("No active aircraft available to retire.");
            return false;
        }

        System.out.printf("%3s  %-12s  %-12s  %-10s%n", "#", "Registration", "Model", "Status");
        int idx = 1;
        for (final Aircraft aircraft : activeAircraft) {
            System.out.printf("%3d) %-12s  %-12s  %-10s%n",
                    idx++, aircraft.registration(), aircraft.modelId(), aircraft.status());
        }

        final int choice = Console.readInteger("Select aircraft by number (0 to cancel):");
        if (choice <= 0 || choice > activeAircraft.size()) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final Aircraft selected = activeAircraft.get(choice - 1);
            controller.retireAircraft(selected.identity());
            System.out.println("Aircraft retired successfully: " + selected.registration());
            return true;
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Retire Aircraft";
    }
}

