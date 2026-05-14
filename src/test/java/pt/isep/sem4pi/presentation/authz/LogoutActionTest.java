package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import org.junit.Test;

import static org.junit.Assert.*;

public class LogoutActionTest {

    @Test
    public void constructsAndImplementsAction() {
        final LogoutAction a = new LogoutAction();
        assertNotNull(a);
        assertTrue(a instanceof Action);
    }
}

