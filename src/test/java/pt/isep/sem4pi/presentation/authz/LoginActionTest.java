package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.*;

/**
 * Teste para a ação de login.
 *
 * Verifica que a ação pode ser construída e implementa a interface Action.
 */
public class LoginActionTest {

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @After
    public void tearDown() {
        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @Test
    public void constructsAndImplementsAction() {
        final LoginAction a1 = new LoginAction();
        final LoginAction a2 = new LoginAction(null);
        assertNotNull(a1);
        assertNotNull(a2);
        assertTrue(Action.class.isAssignableFrom(LoginAction.class));
    }
}