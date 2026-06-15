package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.actions.Action;

public class CreateFlightPlanFromFileAction implements Action {

    @Override
    public boolean execute() {
        return new CreateFlightPlanFromFileUI().show();
    }
}
