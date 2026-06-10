package pt.isep.sem4pi.presentation.flightplan;

import eapli.framework.actions.Action;

public class ListFlightPlansAction implements Action {

    @Override
    public boolean execute() {
        return new ListFlightPlansUI().show();
    }
}
