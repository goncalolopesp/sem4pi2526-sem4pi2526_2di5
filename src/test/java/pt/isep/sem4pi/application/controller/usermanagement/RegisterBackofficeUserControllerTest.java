package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Testes para o controlador de registo de utilizadores do backoffice (US30/US32).
 */
public class RegisterBackofficeUserControllerTest {

    @Before
    public void setUp() {
        // Força o PersistenceContext a usar InMemory para este teste
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());
        PersistenceContext.resetForTesting();

        // Configura o Authz com o mesmo repositório que o controller vai usar
        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        AuthzRegistry.authenticationService().authenticate("admin", "Password1");
        new ManageBackofficeUserStatusController().enableUser("bo");
        AuthzRegistry.authorizationService().clearSession();
    }

    @After
    public void tearDown() {
        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @Test
    public void usernameAndEmailChecksAndRegister() {
        final RegisterBackofficeUserController ctrl = new RegisterBackofficeUserController();

        assertFalse(ctrl.isUsernameAvailable("bo"));
        assertFalse(ctrl.isUsernameAvailable(null));
        assertFalse(ctrl.isUsernameAvailable("   "));

        assertFalse(ctrl.isEmailAvailable(null));
        assertFalse(ctrl.isEmailAvailable("   "));

        assertTrue(ctrl.isEmailFormatValid("joe@example.org"));
        assertFalse(ctrl.isEmailFormatValid("bad-email"));

        assertFalse(ctrl.isEmailFormatValid(null));

        assertFalse(ctrl.isEmailAvailable("bo@example.org"));

        assertTrue(ctrl.isPasswordValid("Password1"));
        assertFalse(ctrl.isPasswordValid("weak"));
        assertFalse(ctrl.isPasswordValid(null));

        assertFalse(ctrl.findBackofficeUserByUsername(null).isPresent());

        final AuthenticationService auth = AuthzRegistry.authenticationService();
        auth.authenticate("admin", "Password1");

        final String username = "newuser" + System.nanoTime();
        final String email = username + "@example.org";
        final Optional<BackofficeUser> before = ctrl.findBackofficeUserByUsername(username);
        assertFalse(before.isPresent());

        final BackofficeUser created = ctrl.register(
                username,
                "Password1",
                "First",
                "Last",
                email,
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
        assertNotNull(created);
        assertEquals(username, created.username());

        final Optional<BackofficeUser> after = ctrl.findBackofficeUserByUsername(username);
        assertTrue(after.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureAdminWithoutSessionThrows() {
        AuthzRegistry.authorizationService().clearSession();

        final RegisterBackofficeUserController ctrl = new RegisterBackofficeUserController();
        ctrl.ensureAdmin();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsWeakPasswordBeforeServiceCall() {
        final RegisterBackofficeUserController ctrl = new RegisterBackofficeUserController();
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        ctrl.register(
                "anotheruser" + System.nanoTime(),
                "weak",
                "First",
                "Last",
                "anotheruser@example.org",
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }
}
