package pt.isep.sem4pi.presentation.authz;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Teste simples para a UI de mudar password (US31).
 *
 * Apenas valida o headline. A troca de password depende de input por consola e do AuthenticationService,
 * por isso deve ser validada de forma integrada ou por testes do serviço.
 */
public class ChangePasswordUITest {

    @Test
    public void headline_isCorrect() {
        final ChangePasswordUI ui = new ChangePasswordUI();
        assertEquals("Change Password", ui.headline());
    }
}
