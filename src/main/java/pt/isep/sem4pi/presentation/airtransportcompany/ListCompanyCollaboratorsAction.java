package pt.isep.sem4pi.presentation.airtransportcompany;

import eapli.framework.actions.Action;

public class ListCompanyCollaboratorsAction implements Action {

    @Override
    public boolean execute() {
        ListCompanyCollaboratorsUI ui = new ListCompanyCollaboratorsUI();
        return ui.show();
    }
}