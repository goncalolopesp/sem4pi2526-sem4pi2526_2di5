package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Teste para a ação de logout.
 *
 * Verifica construção e implementação da interface Action.
 */
public class LogoutActionTest {

    @Test
    public void constructsAndImplementsAction() {
        final LogoutAction a = new LogoutAction();
        assertNotNull(a);
        assertTrue(a instanceof Action);
    }
}
