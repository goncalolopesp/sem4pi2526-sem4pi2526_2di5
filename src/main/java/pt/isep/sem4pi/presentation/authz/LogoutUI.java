package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;

/**
 * Logout UI adapted from example project.
 */
public class LogoutUI extends AbstractUI {
    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    @Override
    public String headline() {
        return "Logout successful!!\n Make a new Login";
    }

    @Override
    protected boolean doShow() {
        authz.clearSession();
        return true;
    }
}

