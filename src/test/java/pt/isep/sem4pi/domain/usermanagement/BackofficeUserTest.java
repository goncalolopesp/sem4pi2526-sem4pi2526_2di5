package pt.isep.sem4pi.domain.usermanagement;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class BackofficeUserTest {

    @Test
    public void constructorAndGetters_andEnableToggle() {
        final BackofficeUser u = new BackofficeUser("joe", "Joe Bloggs", "joe@example.org", "+351900000000", LocalDate.now().plusYears(1), LocalDate.now());
        assertEquals("joe", u.username());
        assertEquals("joe@example.org", u.email());
        assertTrue(u.isEnabled());
        u.setEnabled(false);
        assertFalse(u.isEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalidInputs_throw() {
        new BackofficeUser(null, "", "a@b.c", "123", LocalDate.now(), LocalDate.now());
    }
}

