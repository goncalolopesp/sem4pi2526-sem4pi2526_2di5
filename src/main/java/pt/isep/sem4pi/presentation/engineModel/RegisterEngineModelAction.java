package pt.isep.sem4pi.presentation.engineModel;

import eapli.framework.actions.Action;

public class RegisterEngineModelAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterEngineModelUI().show();
    }
}
