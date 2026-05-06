package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.actions.Action;

public class AddAircraftModelAction implements Action {
    @Override
    public boolean execute() {
        return new AddAircraftModelUI().show();
    }
}