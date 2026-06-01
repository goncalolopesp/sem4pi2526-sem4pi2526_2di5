package pt.isep.sem4pi.presentation.route;

import eapli.framework.actions.Action;

public class CreateFlightRouteAction implements Action {

    /**
     * Executes the action to add a new flight route.
     * <p>This method instantiates and shows the CreateFlightRouteUI.</p>
     *
     * @return true if the UI operation completes successfully, false otherwise
     */
    @Override
    public boolean execute() {
        return new CreateFlightRouteUI().show();
    }
}
