package pt.isep.sem4pi.presentation.usermanagement;

import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ListBackofficeUsersUI extends AbstractUI {

    private final UserStatusService service = new UserStatusService();

    @Override
    protected boolean doShow() {
        try {
            final List<BackofficeUser> users = service.listAll();
            if (users.isEmpty()) {
                System.out.println("No backoffice users available.");
                return true;
            }

            System.out.printf("%-15s  %-30s  %-8s%n", "Username", "Email", "Status");
            for (final BackofficeUser u : users) {
                System.out.printf("%-15s  %-30s  %-8s%n", u.username(), u.email(), u.isEnabled() ? "ENABLED" : "DISABLED");
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

