package pt.isep.sem4pi.application.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.controller.usermanagement.RegisterBackofficeUserController;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Testes para o controlador de registo de utilizadores do backoffice (US30/US32).
 *
 * Verifica disponibilidade de username/email, politica de password e o registo efetivamente cria o utilizador.
 */
public class RegisterBackofficeUserControllerTest {

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }

    /**
     * Verifica checks de username/email/password e registo com conta admin ativa.
     */
    @Test
    public void usernameAndEmailChecksAndRegister() {
        final RegisterBackofficeUserController ctrl = new RegisterBackofficeUserController();

        assertFalse(ctrl.isUsernameAvailable("bo"));
        assertFalse(ctrl.isUsernameAvailable(null));
        assertFalse(ctrl.isUsernameAvailable("   "));

        assertTrue(ctrl.isEmailFormatValid("joe@example.org"));
        assertFalse(ctrl.isEmailFormatValid("bad-email"));

        assertFalse(ctrl.isEmailAvailable("bo@example.org"));

        assertTrue(ctrl.isPasswordValid("Password1"));
        assertFalse(ctrl.isPasswordValid("weak"));

        final AuthenticationService auth = AuthzRegistry.authenticationService();
        auth.authenticate("admin", "Password1", new Role[0]);

        final String username = "newuser";
        final Optional<BackofficeUser> before = ctrl.findBackofficeUserByUsername(username);
        assertFalse(before.isPresent());

        final BackofficeUser created = ctrl.register(
                username,
                "Password1",
                "First",
                "Last",
                "newuser@example.org",
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
        assertNotNull(created);
        assertEquals(username, created.username());

        final Optional<BackofficeUser> after = ctrl.findBackofficeUserByUsername(username);
        assertTrue(after.isPresent());
    }
}
