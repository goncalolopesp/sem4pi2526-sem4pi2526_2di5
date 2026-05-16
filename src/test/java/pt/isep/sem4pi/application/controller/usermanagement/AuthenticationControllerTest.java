package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.assertEquals;

public class AuthenticationControllerTest {

    private String testUser;
    private String disabledUser;

    @Before
    public void setUp() {
        final var userRepo = PersistenceContext.repositories().users();
        AuthzRegistry.configure(userRepo, new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        testUser = "usr_" + java.util.UUID.randomUUID().toString().substring(0, 5);
        disabledUser = "dis_" + java.util.UUID.randomUUID().toString().substring(0, 5);

        SystemUserBuilder builder = new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        userRepo.save(builder.withUsername(testUser).withPassword("Password1").withName("Test", "User").withEmail(testUser + "@isep.pt").withRoles(SEM4PIRoles.ADMIN).build());

        var disUser = builder.withUsername(disabledUser).withPassword("Password1").withName("Dis", "User").withEmail(disabledUser + "@isep.pt").withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR).build();
        disUser.deactivate(eapli.framework.time.util.CurrentTimeCalendars.now());
        userRepo.save(disUser);

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }
    }

    @Test
    public void loginWithValidCredentialsReturnsSuccess() {
        final AuthenticationController controller = new AuthenticationController();
        assertEquals(AuthenticationController.LoginResult.SUCCESS, controller.login(testUser, "Password1"));
    }

    @Test
    public void loginWithInvalidCredentialsReturnsInvalidCredentials() {
        final AuthenticationController controller = new AuthenticationController();
        assertEquals(AuthenticationController.LoginResult.INVALID_CREDENTIALS, controller.login(testUser, "wrongPassword"));
    }

    @Test
    public void loginWithDisabledUserReturnsInvalidCredentials() {
        final AuthenticationController controller = new AuthenticationController();
        assertEquals(AuthenticationController.LoginResult.INVALID_CREDENTIALS, controller.login(disabledUser, "Password1"));
    }
}