package pt.isep.sem4pi.presentation.authz;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoginUITest {

    @Test
    public void headline_and_constructors_work() {
        final LoginUI ui1 = new LoginUI();
        final LoginUI ui2 = new LoginUI(3);
        assertEquals("Login", ui1.headline());
        assertEquals("Login", ui2.headline());
    }
}

