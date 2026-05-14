package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import static org.junit.Assert.*;

public class UserStatusServiceTest {

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        AuthzRegistry.authenticationService().authenticate("admin", "Password1");
    }

    @Test
    public void adminCanDisableBackofficeUser() {
        final UserStatusService service = new UserStatusService();

        service.disableUser("bo");

        final var bo = service.listAllWithStatus().stream()
                .filter(u -> u.username().equals("bo"))
                .findFirst();

        assertTrue(bo.isPresent());
        assertFalse(bo.get().isActive());
    }

    @Test
    public void adminCanEnableBackofficeUser() {
        final UserStatusService service = new UserStatusService();

        service.disableUser("bo");
        service.enableUser("bo");

        final var bo = service.listAllWithStatus().stream()
                .filter(u -> u.username().equals("bo"))
                .findFirst();

        assertTrue(bo.isPresent());
        assertTrue(bo.get().isActive());
    }

    @Test(expected = IllegalStateException.class)
    public void adminCannotDisableOwnAccount() {
        final UserStatusService service = new UserStatusService();

        service.disableUser("admin");
    }
}