package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.*;

public class LogoutActionTest {

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        AuthzRegistry.authenticationService().authenticate("admin", "Password1");
    }

    @Test
    public void constructsAndImplementsAction() {
        final LogoutAction a = new LogoutAction();
        assertNotNull(a);
        assertTrue(a instanceof Action);
    }

    @Test
    public void executeLogsOutSuccessfully() {
        final LogoutAction action = new LogoutAction();
        assertTrue(action.execute());
    }
}

