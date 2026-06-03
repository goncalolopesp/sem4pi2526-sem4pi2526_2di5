package pt.isep.sem4pi.presentation.route;

import eapli.framework.actions.Action;

/**
 * Action to launch the Deactivate Flight Route use case (US074).
 */
public class DeactivateFlightRouteAction implements Action {

    @Override
    public boolean execute() {
        return new DeactivateFlightRouteUI().show();
    }
}
