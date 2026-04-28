package pt.isep.sem4pi.application.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class UserStatusServiceTest {

    @BeforeClass
    public static void setupAuth() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }

    @Test
    public void enableDisableUser() {
        final BackofficeUserRepository boRepo = PersistenceContext.repositories().backofficeUsers();

        var opt = boRepo.ofIdentity("bo");
        Assert.assertTrue(opt.isPresent());
        var user = opt.get();
        Assert.assertTrue(user.isEnabled());

        final AuthenticationService auth = AuthzRegistry.authenticationService();
        var authOpt = auth.authenticate("admin", "Password1");
        Assert.assertTrue(authOpt.isPresent());

        final UserStatusService svc = new UserStatusService();
        svc.disableUser("bo");
        user = boRepo.ofIdentity("bo").get();
        Assert.assertFalse(user.isEnabled());

        svc.enableUser("bo");
        user = boRepo.ofIdentity("bo").get();
        Assert.assertTrue(user.isEnabled());
    }
}

