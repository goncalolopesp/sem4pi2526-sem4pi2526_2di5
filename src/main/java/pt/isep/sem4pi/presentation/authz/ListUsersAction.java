package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;

/**
 * Menu action to list Users in the System
 */
public class ListUsersAction implements Action {

    @Override
    public boolean execute () {
        return new ListUsersUI().show();
    }
}

