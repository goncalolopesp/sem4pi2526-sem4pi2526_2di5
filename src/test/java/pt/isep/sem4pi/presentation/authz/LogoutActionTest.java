package pt.isep.sem4pi.presentation.authz;

import eapli.framework.actions.Action;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogoutActionTest {

    @Test
    public void constructsAndImplementsAction() {
        final LogoutAction a = new LogoutAction();
        assertNotNull(a);
        assertTrue(a instanceof Action);
    }
}

