package pt.isep.sem4pi.presentation.airtransportcompany;

import eapli.framework.actions.Action;

public class ListCompanyFleetAction implements Action {

    @Override
    public boolean execute() {
        ListCompanyFleetUI lf = new ListCompanyFleetUI();
        return lf.show();
    }
}
