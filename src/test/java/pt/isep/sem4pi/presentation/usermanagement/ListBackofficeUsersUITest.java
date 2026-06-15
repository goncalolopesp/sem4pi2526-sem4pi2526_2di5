package pt.isep.sem4pi.presentation.usermanagement;

import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.usermanagment.UserStatusService;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.PhoneNumber;
import pt.isep.sem4pi.testsupport.TestSupport;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ListBackofficeUsersUITest {

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

    private ListBackofficeUsersUI newUiWithService(final UserStatusService service) {
        final ListBackofficeUsersUI ui = new ListBackofficeUsersUI();
        TestSupport.setField(ui, "service", service);
        return ui;
    }

    @Test
    public void doShow_returnsTrueWhenListIsEmpty() {
        final UserStatusService service = Mockito.mock(UserStatusService.class);
        Mockito.when(service.listAllWithStatus()).thenReturn(List.of());

        assertTrue(newUiWithService(service).show());
    }

    @Test
    public void doShow_rendersUsersSuccessfully() {
        final UserStatusService service = Mockito.mock(UserStatusService.class);
        Mockito.when(service.listAllWithStatus()).thenReturn(List.of(status("bo", true)));

        assertTrue(newUiWithService(service).show());
    }

    @Test
    public void doShow_returnsFalseWhenListingFails() {
        final UserStatusService service = Mockito.mock(UserStatusService.class);
        Mockito.when(service.listAllWithStatus()).thenThrow(new IllegalStateException("Boom"));

        assertFalse(newUiWithService(service).show());
    }
}
