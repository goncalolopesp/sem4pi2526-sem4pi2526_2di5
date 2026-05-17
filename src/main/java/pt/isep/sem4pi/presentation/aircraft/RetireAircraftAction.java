package pt.isep.sem4pi.presentation.aircraft;

import eapli.framework.actions.Action;

public class RetireAircraftAction implements Action {
    @Override
    public boolean execute() {
        return new RetireAircraftUI().show();
    }
}

