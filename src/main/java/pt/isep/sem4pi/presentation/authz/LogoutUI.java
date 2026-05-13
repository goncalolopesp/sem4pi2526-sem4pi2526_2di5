package pt.isep.sem4pi.presentation.authz;

import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;

/**
 * UI de logout.
 *
 * Apenas chama o controller e apresenta o resultado.
 */
public class LogoutUI extends AbstractUI {

    private final AuthenticationController controller = new AuthenticationController();

    @Override
    public String headline() {
        return "Logout";
    }

    @Override
    protected boolean doShow() {
        controller.logout();
        System.out.println("Logout successful.");
        return true;
    }
}