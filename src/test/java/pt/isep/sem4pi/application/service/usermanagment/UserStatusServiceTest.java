package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.*;

public class UserStatusServiceTest {

    private String testAdmin;
    private String testBo;

    @Before
    public void setUp() {
        final var userRepo = PersistenceContext.repositories().users();
        AuthzRegistry.configure(userRepo, new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        testAdmin = "adm_" + java.util.UUID.randomUUID().toString().substring(0, 5);
        testBo = "bot_" + java.util.UUID.randomUUID().toString().substring(0, 5);

        SystemUserBuilder builder = new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        userRepo.save(builder.withUsername(testAdmin).withPassword("Password1").withName("Admin", "User").withEmail(testAdmin + "@isep.pt").withRoles(SEM4PIRoles.ADMIN).build());
        userRepo.save(builder.withUsername(testBo).withPassword("Password1").withName("BO", "User").withEmail(testBo + "@isep.pt").withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR).build());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
        AuthzRegistry.authenticationService().authenticate(testAdmin, "Password1");
    }

    @Test
    public void adminCanDisableBackofficeUser() {
        final UserStatusService service = new UserStatusService();
        service.disableUser(testBo);

        var userOpt = PersistenceContext.repositories().users().ofIdentity(Username.valueOf(testBo));
        assertTrue(userOpt.isPresent());
        assertFalse(userOpt.get().isActive());
    }

    @Test
    public void adminCanEnableBackofficeUser() {
        final UserStatusService service = new UserStatusService();
        service.disableUser(testBo);
        service.enableUser(testBo);

        var userOpt = PersistenceContext.repositories().users().ofIdentity(Username.valueOf(testBo));
        assertTrue(userOpt.isPresent());
        assertTrue(userOpt.get().isActive());
    }

    @Test(expected = IllegalStateException.class)
    public void adminCannotDisableOwnAccount() {
        final UserStatusService service = new UserStatusService();
        service.disableUser(testAdmin);
    }
}