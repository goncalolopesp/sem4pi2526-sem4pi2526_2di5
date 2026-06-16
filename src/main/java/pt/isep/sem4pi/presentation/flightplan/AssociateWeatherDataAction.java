package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.actions.Action;

/**
 * Acao de menu que abre a interface de consola da US082.
 */
public class AssociateWeatherDataAction implements Action {

    @Override
    public boolean execute() {
        return new AssociateWeatherDataUI().show();
    }
}
