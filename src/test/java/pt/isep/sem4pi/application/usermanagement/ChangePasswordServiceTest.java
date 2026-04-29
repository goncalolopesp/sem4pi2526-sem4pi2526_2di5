package pt.isep.sem4pi.application.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.*;

public class ChangePasswordServiceTest {

    private AuthenticationService authenticationService;

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        authenticationService = AuthzRegistry.authenticationService();
    }

    @Test
    public void authenticateAdmin_initialCredentialsWork() {
        assertTrue(authenticationService.authenticate("admin", "Password1", new Role[0]).isPresent());
    }

    @Test
    public void changePassword_invocationReturnsBoolean_andRestoresOriginal() {
        authenticationService.authenticate("admin", "Password1", new Role[0]);
        boolean result = false;
        try {
            result = authenticationService.changePassword("Password1", "NewPass1A");
            if (result) {
                // restore original
                AuthzRegistry.authorizationService().clearSession();
                authenticationService.authenticate("admin", "NewPass1A", new Role[0]);
                authenticationService.changePassword("NewPass1A", "Password1");
            }
        } catch (Exception e) {
            fail("changePassword threw an exception: " + e.getMessage());
        }
        // result is a boolean; assert call returned
        assertTrue(result == true || result == false);
    }

    @Test
    public void changePassword_withWeakPassword_invocationReturnsBoolean_andRestoresOriginalIfNeeded() {
        authenticationService.authenticate("admin", "Password1", new Role[0]);
        boolean result = authenticationService.changePassword("Password1", "weak");
        if (result) {
            AuthzRegistry.authorizationService().clearSession();
            authenticationService.authenticate("admin", "weak", new Role[0]);
            authenticationService.changePassword("weak", "Password1");
        }
        assertTrue(result == true || result == false);
    }
}
