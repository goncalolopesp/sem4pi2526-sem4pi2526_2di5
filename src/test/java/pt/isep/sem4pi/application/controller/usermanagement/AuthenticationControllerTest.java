package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import static org.junit.Assert.assertEquals;

public class AuthenticationControllerTest {

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @Test
    public void loginWithValidCredentialsReturnsSuccess() {
        final AuthenticationController controller = new AuthenticationController();

        final var result = controller.login("admin", "Password1");

        assertEquals(AuthenticationController.LoginResult.SUCCESS, result);
    }

    @Test
    public void loginWithInvalidCredentialsReturnsInvalidCredentials() {
        final AuthenticationController controller = new AuthenticationController();

        final var result = controller.login("admin", "wrongPassword");

        assertEquals(AuthenticationController.LoginResult.INVALID_CREDENTIALS, result);
    }

    @Test
    public void loginWithDisabledUserReturnsInvalidCredentials() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final var manageController = new ManageBackofficeUserStatusController();
        manageController.disableUser("bo");

        AuthzRegistry.authorizationService().clearSession();

        final AuthenticationController controller = new AuthenticationController();

        final var result = controller.login("bo", "Password1");

        assertEquals(AuthenticationController.LoginResult.INVALID_CREDENTIALS, result);
    }
}