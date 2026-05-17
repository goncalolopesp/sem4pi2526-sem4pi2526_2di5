package pt.isep.sem4pi.presentation.airtransportcompany;

import eapli.framework.actions.Action;

public class RegisterCompanyCollaboratorAction implements Action {

    @Override
    public boolean execute() {
        RegisterCompanyCollaboratorUI rg = new RegisterCompanyCollaboratorUI();
        return rg.show();
    }
}