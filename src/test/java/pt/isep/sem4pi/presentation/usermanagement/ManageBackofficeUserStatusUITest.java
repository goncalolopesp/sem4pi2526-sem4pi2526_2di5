package pt.isep.sem4pi.presentation.usermanagement;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.controller.usermanagement.ManageBackofficeUserStatusController;
import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.PhoneNumber;
import pt.isep.sem4pi.testsupport.TestSupport;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManageBackofficeUserStatusUITest {

    private InputStream originalIn;

    @After
    public void tearDown() {
        if (originalIn != null) {
            System.setIn(originalIn);
        }
    }

    private ManageBackofficeUserStatusUI newUiWithController(final ManageBackofficeUserStatusController controller) {
        final ManageBackofficeUserStatusUI ui = new ManageBackofficeUserStatusUI();
        TestSupport.setField(ui, "controller", controller);
        return ui;
    }

    private static UserStatusService.BackofficeUserStatus status(final String username, final boolean active) {
        final BackofficeUser profile = new BackofficeUser(
                username,
                username + " Name",
                Email.valueOf(username + "@example.org"),
                PhoneNumber.valueOf("+351900000000"),
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
        return new UserStatusService.BackofficeUserStatus(profile, active);
    }

    @Test
    public void doShow_returnsTrueWhenListIsEmpty() {
        final ManageBackofficeUserStatusController controller = Mockito.mock(ManageBackofficeUserStatusController.class);
        Mockito.when(controller.listAllWithStatus()).thenReturn(List.of());

        assertTrue(newUiWithController(controller).show());
    }

    @Test
    public void doShow_cancelsWhenChoiceIsZero() {
        originalIn = System.in;
        System.setIn(TestSupport.consoleInput("0"));

        final ManageBackofficeUserStatusController controller = Mockito.mock(ManageBackofficeUserStatusController.class);
        Mockito.when(controller.listAllWithStatus()).thenReturn(List.of(status("bo", true)));

        assertTrue(newUiWithController(controller).show());
    }

    @Test
    public void doShow_returnsFalseWhenListingFails() {
        final ManageBackofficeUserStatusController controller = Mockito.mock(ManageBackofficeUserStatusController.class);
        Mockito.when(controller.listAllWithStatus()).thenThrow(new IllegalStateException("Boom"));

        assertFalse(newUiWithController(controller).show());
    }
}

