package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;

/**
 * UI de logout.
 *
 * Limpa a sessao atual e retorna sucesso. Usada quando o utilizador termina a sessao.
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
