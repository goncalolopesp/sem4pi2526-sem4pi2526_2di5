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

public class UserStatusServiceTest {

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }

    @Test
    public void disableAndEnableUser_updatesRepository() {
        final AuthenticationService auth = AuthzRegistry.authenticationService();
        final AuthorizationService authz = AuthzRegistry.authorizationService();
        final Optional<?> login = auth.authenticate("admin", "Password1", new Role[0]);
        assertTrue("admin should authenticate", login.isPresent());
        assertTrue("admin should have ADMIN role", authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN));

        final UserStatusService svc = new UserStatusService();
        svc.disableUser("bo");
        final var repo = PersistenceContext.repositories().backofficeUsers();
        final var opt = repo.ofIdentity("bo");
        assertTrue(opt.isPresent());
        assertFalse(opt.get().isEnabled());
        svc.enableUser("bo");
        final var opt2 = repo.ofIdentity("bo");
        assertTrue(opt2.isPresent());
        assertTrue(opt2.get().isEnabled());
    }
}
