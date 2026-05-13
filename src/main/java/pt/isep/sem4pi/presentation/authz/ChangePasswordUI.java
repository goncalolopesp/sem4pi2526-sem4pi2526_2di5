package pt.isep.sem4pi.presentation.authz;

import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

/**
 * UI para alteração de password.
 *
 * Lê a password antiga e a nova da consola e pede ao serviço de autenticação para mudar a password.
 * A UI trata mensagens de sucesso/erro. A lógica de negócio fica no AuthenticationService.
 */
@SuppressWarnings("squid:S106")
public class ChangePasswordUI extends AbstractUI {

    private final AuthenticationService authenticationService = AuthzRegistry.authenticationService();

    @Override
    protected boolean doShow() {
        final String oldPassword = Console.readLine("Old Password:");
        final String newPassword = Console.readLine("New Password:");

        try {
            if (this.authenticationService.changePassword(oldPassword, newPassword)) {
                System.out.println("Password Successfully changed");
                return true;
            } else {
                System.out.println("Invalid authentication");
                return false;
            }
        } catch (ConcurrencyException | IntegrityViolationException e) {
            System.out.println("An error has occurred> " + e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Change Password";
    }
}
