package pt.isep.sem4pi.presentation.aircraftModel;

import eapli.framework.actions.Action;

public class CertifyEngineModelAction implements Action {
    @Override
    public boolean execute() {
        return new CertifyEngineModelUI().show();
    }
}

