package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;

/**
 * Acao do menu para efectuar logout.
 *
 * Invoca a UI de logout que limpa a sessao actual.
 */
public class LogoutAction implements Action {

    @Override
    public boolean execute() {
        return new LogoutUI().show();
    }
}
