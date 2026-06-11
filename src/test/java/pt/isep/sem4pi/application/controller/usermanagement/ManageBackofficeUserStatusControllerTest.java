package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.controller.usermanagement.ManageBackofficeUserStatusController;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import static org.junit.Assert.*;

public class ManageBackofficeUserStatusControllerTest {

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());
        PersistenceContext.resetForTesting();

        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        AuthzRegistry.authenticationService().authenticate("admin", "Password1");
    }

    @Test
    public void disableAndEnableUser_changesStatus() {
        final var controller = new ManageBackofficeUserStatusController();

        controller.disableUser("bo");

        var users = controller.listAllWithStatus();
        var bo = users.stream()
                .filter(u -> u.username().equals("bo"))
                .findFirst();

        assertTrue(bo.isPresent());
        assertFalse(bo.get().isActive());

        controller.enableUser("bo");

        users = controller.listAllWithStatus();
        bo = users.stream()
                .filter(u -> u.username().equals("bo"))
                .findFirst();

        assertTrue(bo.isPresent());
        assertTrue(bo.get().isActive());
    }
}
