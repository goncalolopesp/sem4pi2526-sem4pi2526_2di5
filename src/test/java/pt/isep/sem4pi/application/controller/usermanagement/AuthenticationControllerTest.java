package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.usermanagment.UserAccessService;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;
import pt.isep.sem4pi.testsupport.TestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AuthenticationControllerTest {

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

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

    @Test
    public void loginWithAccessDeniedClearsSessionAndReturnsAccessDenied() {
        final AuthenticationController controller = new AuthenticationController();
        final UserAccessService accessService = Mockito.mock(UserAccessService.class);
        Mockito.when(accessService.canUserAccess("admin")).thenReturn(false);
        TestSupport.setField(controller, "userAccessService", accessService);

        final var result = controller.login("admin", "Password1");

        assertEquals(AuthenticationController.LoginResult.ACCESS_DENIED, result);
        assertFalse(AuthzRegistry.authorizationService().hasSession());
    }

    @Test
    public void logoutWithoutSessionDoesNothing() {
        AuthzRegistry.authorizationService().clearSession();

        final AuthenticationController controller = new AuthenticationController();
        controller.logout();

        assertFalse(AuthzRegistry.authorizationService().hasSession());
    }

    @Test(expected = IllegalStateException.class)
    public void changePasswordWithoutSessionThrows() {
        AuthzRegistry.authorizationService().clearSession();

        final AuthenticationController controller = new AuthenticationController();
        controller.changePassword("Password1", "Password2A");
    }

    @Test
    public void changePasswordRejectsWeakPassword() {
        final AuthenticationController controller = new AuthenticationController();
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final var result = controller.changePassword("Password1", "weak");

        assertEquals(AuthenticationController.ChangePasswordResult.PASSWORD_POLICY_VIOLATION, result);
    }

    @Test
    public void changePasswordWithWrongOldPasswordReturnsSuccess() {
        final AuthenticationController controller = new AuthenticationController();
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final var result = controller.changePassword("wrongPassword", "Password2A");

        assertEquals(AuthenticationController.ChangePasswordResult.SUCCESS, result);
    }

    @Test
    public void changePasswordWithValidCredentialsReturnsSuccessAndCanBeRestored() {
        final AuthenticationController controller = new AuthenticationController();
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final var result = controller.changePassword("Password1", "Password2A");

        assertEquals(AuthenticationController.ChangePasswordResult.SUCCESS, result);

        final var restoreResult = controller.changePassword("Password2A", "Password1");
        assertEquals(AuthenticationController.ChangePasswordResult.SUCCESS, restoreResult);
    }
}