package pt.isep.sem4pi.presentation.authz;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testes básicos para a UI de login.
 *
 * Valida propriedades simples como o título (headline). A leitura de credenciais é feita por consola
 * e não é testada automaticamente aqui.
 */
public class LoginUITest {

    @Test
    public void headline_and_constructors_work() {
        final LoginUI ui1 = new LoginUI();
        final LoginUI ui2 = new LoginUI(3);
        assertEquals("Login", ui1.headline());
        assertEquals("Login", ui2.headline());
    }
}
