package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryBackofficeUserRepository;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Testes para o repositório em memória de BackofficeUser.
 *
 * A entidade BackofficeUser é apenas perfil; o estado ativo/inativo é do SystemUser.
 */
public class InMemoryBackofficeUserRepositoryTest {

    private InMemoryBackofficeUserRepository repo;

    @Before
    public void setUp() {
        repo = new InMemoryBackofficeUserRepository();
    }

    /**
     * Verifica que um utilizador salvo é recuperado corretamente.
     */
    @Test
    public void saveAndFind_returnsSavedUser() {
        final BackofficeUser u = new BackofficeUser("joe", "Joe", "joe@example.org",
                "+351999999999", LocalDate.now().plusYears(1), LocalDate.now());
        repo.save(u);

        final var stored = repo.ofIdentity("joe");
        assertTrue(stored.isPresent());
        assertEquals("joe", stored.get().username());
        assertEquals("joe@example.org", stored.get().email());
    }

    /**
     * Verifica que guardar um utilizador existente actualiza o estado (overwrite).
     */
    @Test
    public void save_overwritesExistingUser() {
        final BackofficeUser u = new BackofficeUser("amy", "Amy", "amy@example.org",
                "+351111111111", LocalDate.now().plusYears(1), LocalDate.now());
        repo.save(u);

        final var opt = repo.ofIdentity("amy");
        assertTrue(opt.isPresent());

        final BackofficeUser updated = new BackofficeUser("amy", "Amy Updated", "amy@example.org",
                "+351111111111", LocalDate.now().plusYears(1), LocalDate.now());
        repo.save(updated);

        final var opt2 = repo.ofIdentity("amy");
        assertTrue(opt2.isPresent());
        assertEquals("Amy Updated", opt2.get().name());
    }
}
