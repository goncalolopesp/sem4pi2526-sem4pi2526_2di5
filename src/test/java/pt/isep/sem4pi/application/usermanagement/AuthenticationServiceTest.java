package pt.isep.sem4pi.application.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.Optional;

import static org.junit.Assert.*;

public class AuthenticationServiceTest {

    private AuthenticationService authenticationService;
    private AuthorizationService authorizationService;

    @Before
    public void setUp() {
        // configure framework authz to use in-memory repositories seeded by PersistenceContext
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        authenticationService = AuthzRegistry.authenticationService();
        authorizationService = AuthzRegistry.authorizationService();
    }

    @Test
    public void authenticateWithValidCredentials_returnsPresent() {
        final Optional<?> opt = authenticationService.authenticate("admin", "Password1", new Role[0]);
        assertTrue(opt.isPresent());
        // after authentication, authorization service must recognize ADMIN role
        assertTrue(authorizationService.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN));
    }

    @Test
    public void authenticateWithInvalidPassword_returnsEmpty() {
        final Optional<?> opt = authenticationService.authenticate("admin", "bad", new Role[0]);
        assertFalse(opt.isPresent());
    }

    @Test
    public void authenticateWithNonExistingUser_returnsEmpty() {
        final Optional<?> opt = authenticationService.authenticate("noone", "x", new Role[0]);
        assertFalse(opt.isPresent());
    }

    @Test
    public void operatorDoesNotHaveAdminRole() {
        // authenticate backoffice operator 'bo' seeded with Password1
        authorizationService.clearSession();
        final Optional<?> opt = authenticationService.authenticate("bo", "Password1", new Role[0]);
        assertTrue(opt.isPresent());
        assertFalse("Operator should not have ADMIN role", authorizationService.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN));
    }

    @Test
    public void disablingBackofficeUser_updatesEnabledFlag() {
        // ensure admin session to disable
        authorizationService.clearSession();
        authenticationService.authenticate("admin", "Password1", new Role[0]);
        final UserStatusService svc = new UserStatusService();
        svc.disableUser("bo");
        final var repo = PersistenceContext.repositories().backofficeUsers();
        final var opt = repo.ofIdentity("bo");
        assertTrue(opt.isPresent());
        assertFalse(opt.get().isEnabled());
        // re-enable for test isolation
        svc.enableUser("bo");
        final var opt2 = repo.ofIdentity("bo");
        assertTrue(opt2.isPresent());
        assertTrue(opt2.get().isEnabled());
    }
}
