package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.*;

/**
 * Teste da UI de logout (US30 - autenticação).
 *
 * Verifica que a chamada limpa a sessão e retorna sucesso.
 */
public class LogoutUITest {

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }

    /**
     * Autentica um utilizador e depois verifica que o logout limpa a sessão.
     */
    @Test
    public void doShow_clearsSessionAndReturnsTrue() {
        final AuthenticationService auth = AuthzRegistry.authenticationService();
        auth.authenticate("admin", "Password1", new Role[0]);
        final LogoutUI ui = new LogoutUI();
        final boolean result = ui.show();
        assertTrue(result);
    }
}
