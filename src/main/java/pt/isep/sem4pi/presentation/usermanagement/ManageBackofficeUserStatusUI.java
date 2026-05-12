package pt.isep.sem4pi.presentation.usermanagement;

import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ManageBackofficeUserStatusUI extends AbstractUI {

    private final UserStatusService service = new UserStatusService();

    @Override
    protected boolean doShow() {
        try {
            final List<BackofficeUser> users = service.listAll();
            if (users.isEmpty()) {
                System.out.println("No backoffice users available.");
                return true;
            }

            System.out.printf("%3s  %-15s  %-30s  %-8s%n", "#", "Username", "Email", "Status");
            int idx = 1;
            for (final BackofficeUser u : users) {
                System.out.printf("%3d) %-15s  %-30s  %-8s%n", idx++, u.username(), u.email(), u.isEnabled() ? "ENABLED" : "DISABLED");
            }

            final int choice = Console.readInteger("Select user by number (0 to cancel):");
            if (choice <= 0 || choice > users.size()) {
                System.out.println("Operation cancelled.");
                return true;
            }

            final BackofficeUser selected = users.get(choice - 1);
            System.out.println("Selected: " + selected.username() + " (" + (selected.isEnabled() ? "ENABLED" : "DISABLED") + ")");
            final String action = Console.readLine("Type 'enable' to enable, 'disable' to disable, or enter to cancel:");
            if (action == null || action.trim().isEmpty()) {
                System.out.println("Operation cancelled.");
                return true;
            }
            final String a = action.trim().toLowerCase();
            if (a.equals("enable")) {
                service.enableUser(selected.username());
                System.out.println("User enabled.");
            } else if (a.equals("disable")) {
                try {
                    service.disableUser(selected.username());
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

