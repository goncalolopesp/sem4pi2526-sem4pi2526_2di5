package pt.isep.sem4pi.presentation.authz;

import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;

/**
 * UI para alteração de password.
 *
 * A UI recolhe dados e delega a operação no controller.
 */
@SuppressWarnings("squid:S106")
public class ChangePasswordUI extends AbstractUI {

    private final AuthenticationController controller = new AuthenticationController();

    @Override
    protected boolean doShow() {
        final String oldPassword = Console.readLine("Old Password:");
        final String newPassword = Console.readLine("New Password:");

        try {
            final AuthenticationController.ChangePasswordResult result =
                    controller.changePassword(oldPassword, newPassword);

            if (result == AuthenticationController.ChangePasswordResult.SUCCESS) {
                System.out.println("Password successfully changed.");
                return true;
            }

            if (result == AuthenticationController.ChangePasswordResult.PASSWORD_POLICY_VIOLATION) {
                System.out.println("Invalid password. It must have at least 6 characters, one digit and one uppercase letter.");
                return false;
            }

            System.out.println("Invalid authentication.");
            return false;

        } catch (final ConcurrencyException | IntegrityViolationException e) {
            System.out.println("The operation could not be completed: " + e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Change Password";
    }
}