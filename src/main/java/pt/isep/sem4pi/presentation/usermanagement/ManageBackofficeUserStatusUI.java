package pt.isep.sem4pi.presentation.usermanagement;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.usermanagement.ManageBackofficeUserStatusController;
import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;

import java.util.List;

public class ManageBackofficeUserStatusUI extends AbstractUI {

    private final ManageBackofficeUserStatusController controller = new ManageBackofficeUserStatusController();

    @Override
    protected boolean doShow() {
        try {
            final List<UserStatusService.BackofficeUserStatus> users = controller.listAllWithStatus();
            if (users.isEmpty()) {
                System.out.println("No backoffice users available.");
                return true;
            }

            System.out.printf("%3s  %-15s  %-30s  %-8s%n", "#", "Username", "Email", "Status");
            int idx = 1;
            for (final UserStatusService.BackofficeUserStatus u : users) {
                System.out.printf("%3d) %-15s  %-30s  %-8s%n", idx++, u.username(), u.email(), u.isActive() ? "ENABLED" : "DISABLED");
            }

            final String choiceRaw = Console.readNonEmptyLine("Select user by number (0 to cancel):", "A selection is required");
            final int choice;
            try {
                choice = Integer.parseInt(choiceRaw.trim());
            } catch (final NumberFormatException e) {
                System.out.println("Operation cancelled.");
                return true;
            }

            if (choice <= 0 || choice > users.size()) {
                System.out.println("Operation cancelled.");
                return true;
            }

            final UserStatusService.BackofficeUserStatus selected = users.get(choice - 1);
            System.out.println("Selected: " + selected.username() + " (" + (selected.isActive() ? "ENABLED" : "DISABLED") + ")");
            final String action = Console.readNonEmptyLine("Type 'enable' to enable, 'disable' to disable, or enter to cancel:", "Action is required");
            if (action == null || action.trim().isEmpty()) {
                System.out.println("Operation cancelled.");
                return true;
            }
            final String a = action.trim().toLowerCase();
            if (a.equals("enable")) {
                controller.enableUser(selected.username());
                System.out.println("User enabled.");
            } else if (a.equals("disable")) {
                try {
                    controller.disableUser(selected.username());
                    System.out.println("User disabled.");
                } catch (final Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Unknown action. Cancelled.");
            }

            return true;
        } catch (final Exception e) {
            System.out.println("Error managing user status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Manage Backoffice User Status";
    }
}