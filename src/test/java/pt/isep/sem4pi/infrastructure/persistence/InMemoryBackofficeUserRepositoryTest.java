package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class InMemoryBackofficeUserRepositoryTest {

    private InMemoryBackofficeUserRepository repo;

    @Before
    public void setUp() {
        repo = new InMemoryBackofficeUserRepository();
    }

    @Test
    public void saveAndFind_returnsSavedUser() {
        BackofficeUser u = new BackofficeUser("joe", "Joe", "joe@example.org",
                "+351999999999", LocalDate.now().plusYears(1), LocalDate.now());
        repo.save(u);

        final var stored = repo.ofIdentity("joe");
        assertTrue(stored.isPresent());
        assertEquals("joe", stored.get().username());
        assertTrue(stored.get().isEnabled());
    }

    @Test
    public void save_overwritesExistingUser() {
        BackofficeUser u = new BackofficeUser("amy", "Amy", "amy@example.org",
                "+351111111111", LocalDate.now().plusYears(1), LocalDate.now());
        repo.save(u);
        // update enabled flag via setEnabled
        final var opt = repo.ofIdentity("amy");
        assertTrue(opt.isPresent());
        final BackofficeUser found = opt.get();
        found.setEnabled(false);
        repo.save(found);

        final var opt2 = repo.ofIdentity("amy");
        assertTrue(opt2.isPresent());
        assertFalse(opt2.get().isEnabled());
    }
}
