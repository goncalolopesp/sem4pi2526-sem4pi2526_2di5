package pt.isep.sem4pi.presentation.usermanagement;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.controller.usermanagement.RegisterBackofficeUserController;
import pt.isep.sem4pi.testsupport.TestSupport;

import java.io.InputStream;
import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegisterBackofficeUserUITest {

    private InputStream originalIn;

    @After
    public void tearDown() {
        if (originalIn != null) {
            System.setIn(originalIn);
        }
    }

    private RegisterBackofficeUserUI newUiWithController(final RegisterBackofficeUserController controller) {
        final RegisterBackofficeUserUI ui = new RegisterBackofficeUserUI();
        TestSupport.setField(ui, "controller", controller);
        return ui;
    }

    @Test
    public void doShow_returnsFalseWhenControllerRejectsAccess() {
        final RegisterBackofficeUserController controller = Mockito.mock(RegisterBackofficeUserController.class);
        Mockito.doThrow(new IllegalStateException("Access denied")).when(controller).ensureAdmin();

        assertFalse(newUiWithController(controller).show());
    }
}
