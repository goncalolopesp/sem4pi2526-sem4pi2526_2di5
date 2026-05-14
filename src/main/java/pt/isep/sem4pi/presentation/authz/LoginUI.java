package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.usermanagement.AuthenticationController;

/**
 * UI de login.
 *
 * Apenas recolhe input e apresenta resultados.
 * A autenticação e regras de acesso são coordenadas pelo controller.
 */
@SuppressWarnings("squid:S106")
public class LoginUI extends AbstractUI {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final Role onlyWithThis;
    private final int maxAttempts;
    private final AuthenticationController controller = new AuthenticationController();

    public LoginUI() {
        this.onlyWithThis = null;
        this.maxAttempts = DEFAULT_MAX_ATTEMPTS;
    }

    public LoginUI(final Role onlyWithThis) {
        this.onlyWithThis = onlyWithThis;
        this.maxAttempts = DEFAULT_MAX_ATTEMPTS;
    }

    public LoginUI(final Role onlyWithThis, final int maxAttempts) {
        this.onlyWithThis = onlyWithThis;
        this.maxAttempts = maxAttempts;
    }

    public LoginUI(final int maxAttempts) {
        this.onlyWithThis = null;
        this.maxAttempts = maxAttempts;
    }

    @Override
    protected boolean doShow() {
        var attempt = 1;

        while (attempt <= maxAttempts) {
            final String username = Console.readNonEmptyLine("Username:", "Please provide a username");
            final String password = Console.readLine("Password:");

            final AuthenticationController.LoginResult result =
                    controller.login(username, password, onlyWithThis);

            if (result == AuthenticationController.LoginResult.SUCCESS) {
                return true;
            }

            if (result == AuthenticationController.LoginResult.ACCESS_DENIED) {
                System.out.println("Your account is disabled. Please contact an administrator.");
                return false;
            }

            System.out.printf("Wrong username or password. You have %d attempts left.%n%n",
                    maxAttempts - attempt);

            attempt++;
        }

        System.out.println("Sorry, we are unable to authenticate you. Please contact your system administrator.");
        return false;
    }

    @Override
    public String headline() {
        return "Login";
    }
}