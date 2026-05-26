package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.testsupport.TestSupport;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Testes básicos para a UI de login.
 *
 * Valida propriedades simples como o título (headline). A leitura de credenciais é feita por consola
 * e não é testada automaticamente aqui.
 */
public class LoginUITest {

    private InputStream originalIn;

    private static final class StubAuthenticationController extends AuthenticationController {
        private final LoginResult loginResult;

        private StubAuthenticationController(final LoginResult loginResult) {
            this.loginResult = loginResult;
        }

        @Override
        public LoginResult login(final String username, final String password, final eapli.framework.infrastructure.authz.domain.model.Role... requiredRoles) {
            return loginResult;
        }
    }

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @After
    public void tearDown() {
        if (originalIn != null) {
            System.setIn(originalIn);
        }

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    private LoginUI newUiWithController(final AuthenticationController controller, final int maxAttempts) {
        final LoginUI ui = new LoginUI(maxAttempts);
        TestSupport.setField(ui, "controller", controller);
        return ui;
    }

    @Test
    public void headline_and_constructors_work() {
        final LoginUI ui1 = new LoginUI();
        final LoginUI ui2 = new LoginUI(3);
        assertEquals("Login", ui1.headline());
        assertEquals("Login", ui2.headline());
    }
}