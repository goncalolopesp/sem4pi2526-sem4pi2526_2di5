package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.actions.Action;

/**
 * Menu action that opens the US082 console interface.
 */
public class AssociateWeatherDataAction implements Action {

    @Override
    public boolean execute() {
        return new AssociateWeatherDataUI().show();
    }
}
