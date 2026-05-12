package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

/**
 * Interface de utilizador para login.
 *
 * Le username password da consola e tenta autenticar. Se o utilizador estiver
 * desativado informa e impede o acesso.
 */
@SuppressWarnings("squid:S106")
public class LoginUI extends AbstractUI {

    private final Role onlyWithThis;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private final int maxAttempts;

    private final AuthenticationService authenticationService = AuthzRegistry.authenticationService();
    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public LoginUI() {
        maxAttempts = DEFAULT_MAX_ATTEMPTS;
        this.onlyWithThis = null;
    }

    public LoginUI(final Role onlyWithThis) {
        this.onlyWithThis = onlyWithThis;
        maxAttempts = DEFAULT_MAX_ATTEMPTS;
    }

    public LoginUI(final Role onlyWithThis, final int maxAttempts) {
        this.onlyWithThis = onlyWithThis;
        this.maxAttempts = maxAttempts;
    }

    public LoginUI(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.onlyWithThis = null;
    }

    @Override
    protected boolean doShow() {
        var attempt = 1;
        while (attempt <= maxAttempts) {
            final String userName = Console.readNonEmptyLine("Username:", "Please provide a username");
            final String password = Console.readLine("Password:");

            if (authenticationService.authenticate(userName, password, onlyWithThis).isPresent()) {
                final var repos = PersistenceContext.repositories();
                if (repos != null) {
                    final var boRepo = repos.backofficeUsers();
                    if (boRepo != null) {
                        final var opt = boRepo.ofIdentity(userName);
                        if (opt.isPresent() && !opt.get().isEnabled()) {
                            if (authz != null) {
                                authz.clearSession();
                            }
                            System.out.println("Your account has been disabled. Please contact an administrator.");
                            return false;
                        }
                    }
                }
                return true;
            }
            System.out.printf("Wrong username or password. You have %d attempts left.%n%n»»»»»»»»»%n",
                    maxAttempts - attempt);
            attempt++;
        }
        System.out.println("Sorry, we are unable to authenticate you. Please contact your system admnistrator.");
        return false;
    }

    @Override
    public String headline() {
        return "Login";
    }
}
