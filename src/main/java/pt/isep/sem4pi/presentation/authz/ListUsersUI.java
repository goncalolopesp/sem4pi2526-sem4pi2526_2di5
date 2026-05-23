package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.usermanagement.ListUsersController;

import java.util.stream.Collectors;

/**
 * UI to list users in the system. Shows username, name, roles and status (active/inactive).
 */

public class ListUsersUI extends AbstractUI {
    private final ListUsersController controller = new ListUsersController();

    @Override
    public boolean doShow() {
        System.out.println("List of users:");
        for (SystemUser user : controller.allBackofficeUsers()) {

            // 1. Get the roles and join them in a String
            String roles = user.roleTypes().stream()
                    .map(Role::toString)
                    .collect(Collectors.joining(", "));

            // 2. Determine status
            String status = user.isActive() ? "ACTIVE" : "INACTIVE";

            // 3. Print the result
            System.out.printf("Username: %-20s | Name: %-30s | Roles: [%s] | Status: %s%n",
                    user.username(),
                    user.name(),
                    roles,
                    status);
        }
        return true;
    }

    @Override
    public String headline() {
        return "List of users";
    }
}
