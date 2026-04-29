package pt.isep.sem4pi.presentation.authz;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChangePasswordUITest {

    @Test
    public void headline_isCorrect() {
        final ChangePasswordUI ui = new ChangePasswordUI();
        assertEquals("Change Password", ui.headline());
    }
}

