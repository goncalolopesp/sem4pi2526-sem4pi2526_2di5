package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;

/**
 * Menu action for user logout adapted from example project.
 */
public class LogoutAction implements Action {

    @Override
    public boolean execute() {
        return new LogoutUI().show();
    }
}

