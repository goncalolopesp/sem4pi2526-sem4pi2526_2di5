package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Test;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import static org.junit.Assert.*;

/**
 * Testes para a fábrica de repositórios em memória.
 */
public class InMemoryRepositoryFactoryTest {

    /**
     * Verifica obtenção da fábrica e dos repositórios e existência do utilizador 'bo' semeado.
     */
    @Test
    public void providesRepositoriesAndSeedsDefaultUsers() {
        final RepositoryFactory f = new InMemoryRepositoryFactory();
        assertNotNull(f);

        final var users = f.users();
        assertNotNull(users);

        final var bos = f.backofficeUsers();
        assertNotNull(bos);

        assertTrue(bos.existsByUsername("bo"));
        assertTrue(bos.existsByUsername("admin"));
    }
}
