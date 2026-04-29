package pt.isep.sem4pi.infrastructure.persistence;

import org.junit.Test;

import static org.junit.Assert.*;

public class InMemoryRepositoryFactoryTest {

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

