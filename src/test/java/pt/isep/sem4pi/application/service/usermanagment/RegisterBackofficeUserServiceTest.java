package pt.isep.sem4pi.application.service.usermanagment;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class RegisterBackofficeUserServiceTest {

    private RegisterBackofficeUserService service;
    private InMemoryRepositoryFactory factory;

    @Before
    public void setUp() {
        factory = new InMemoryRepositoryFactory();
        service = new RegisterBackofficeUserService(factory.users(), factory.backofficeUsers());
    }

    @Test
    public void registerCreatesSystemUserAndBackofficeUser() {
        service.register(
                "newuser",
                "Password1",
                "New",
                "User",
                "newuser@example.org",
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );

        assertTrue(factory.backofficeUsers().existsByUsername("newuser"));
        assertTrue(factory.backofficeUsers().existsByEmail("newuser@example.org"));
        assertTrue(factory.users()
                .ofIdentity(eapli.framework.infrastructure.authz.domain.model.Username.valueOf("newuser"))
                .isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerDoesNotAllowDuplicatedUsername() {
        service.register(
                "bo",
                "Password1",
                "Backoffice",
                "Duplicate",
                "another@example.org",
                "+351900000001",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerDoesNotAllowDuplicatedEmail() {
        service.register(
                "anotheruser",
                "Password1",
                "Another",
                "User",
                "bo@example.org",
                "+351900000002",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }
}