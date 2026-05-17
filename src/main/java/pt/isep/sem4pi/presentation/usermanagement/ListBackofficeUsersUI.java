package pt.isep.sem4pi.presentation.usermanagement;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;

import java.util.List;

public class ListBackofficeUsersUI extends AbstractUI {

    private final UserStatusService service = new UserStatusService();

    @Override
    protected boolean doShow() {
        try {
            final List<UserStatusService.BackofficeUserStatus> users = service.listAllWithStatus();
            if (users.isEmpty()) {
                System.out.println("No backoffice users available.");
                return true;
            }

            System.out.printf("%-15s  %-30s  %-8s%n", "Username", "Email", "Status");
            for (final UserStatusService.BackofficeUserStatus u : users) {
                System.out.printf("%-15s  %-30s  %-8s%n", u.username(), u.email(), u.isActive() ? "ENABLED" : "DISABLED");
            }
            return true;
        } catch (final Exception e) {
            System.out.println("Error listing users: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "List Backoffice Users";
    }
}
