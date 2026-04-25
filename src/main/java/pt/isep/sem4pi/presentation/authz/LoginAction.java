package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;

/**
 * Menu action for user login adapted from example project.
 */
public class LoginAction implements Action {

    private final Role onlyWithThis;

    public LoginAction() {
        onlyWithThis = null;
    }

    public LoginAction(final Role onlyWithThis) {
        this.onlyWithThis = onlyWithThis;
    }

    @Override
    public boolean execute() {
        return new LoginUI(onlyWithThis).show();
    }
}
