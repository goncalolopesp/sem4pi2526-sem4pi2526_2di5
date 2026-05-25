package pt.isep.sem4pi.presentation.authz;

import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.testsupport.TestSupport;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Teste simples para a UI de mudar password (US31).
 *
 * Apenas valida o headline. A troca de password depende de input por consola e do AuthenticationService,
 * por isso deve ser validada de forma integrada ou por testes do serviço.
 */
public class ChangePasswordUITest {

    private InputStream originalIn;

    private ChangePasswordUI newUiWithController(final AuthenticationController controller) {
        final ChangePasswordUI ui = new ChangePasswordUI();
        TestSupport.setField(ui, "controller", controller);
        return ui;
    }

    private void configureAuthz() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @org.junit.After
    public void tearDown() {
        if (originalIn != null) {
            System.setIn(originalIn);
        }

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @Test
    public void headline_isCorrect() {
        final ChangePasswordUI ui = new ChangePasswordUI();
        assertEquals("Change Password", ui.headline());
    }
}