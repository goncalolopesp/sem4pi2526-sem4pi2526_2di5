package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Teste para a ação de login.
 *
 * Verifica que a ação pode ser construída e implementa a interface Action.
 */
public class LoginActionTest {

    @Test
    public void constructsAndImplementsAction() {
        final LoginAction a1 = new LoginAction();
        final LoginAction a2 = new LoginAction(null);
        assertNotNull(a1);
        assertNotNull(a2);
        assertTrue(a1 instanceof Action);
        assertTrue(a2 instanceof Action);
    }
}
