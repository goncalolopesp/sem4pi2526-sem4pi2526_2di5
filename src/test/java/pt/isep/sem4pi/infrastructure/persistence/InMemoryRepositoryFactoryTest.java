package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testes para a fábrica de repositórios em memória.
 *
 * Garante que o PersistenceContext devolve a fábrica e que os repositórios estão semeados.
 */
public class InMemoryRepositoryFactoryTest {

    /**
     * Verifica obtenção da fábrica e dos repositórios e existência do utilizador 'bo' semeado.
     */
    @Test
    public void persistenceContextProvidesFactoryAndRepositories() {
        final RepositoryFactory f = PersistenceContext.repositories();
        assertNotNull(f);

        final var users = f.users();
        assertNotNull(users);

        final var bos = f.backofficeUsers();
        assertNotNull(bos);

        assertTrue(bos.existsByUsername("bo"));
    }
}
