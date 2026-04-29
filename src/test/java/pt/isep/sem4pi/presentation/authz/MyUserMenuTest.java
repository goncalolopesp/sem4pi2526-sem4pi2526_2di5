package pt.isep.sem4pi.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import static org.junit.Assert.*;

public class MyUserMenuTest {

    @Before
    public void setUp() {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }

    @Test
    public void constructsWithAndWithoutSession() {
        final MyUserMenu m1 = new MyUserMenu();
        assertNotNull(m1);

        AuthzRegistry.authenticationService().authenticate("admin", "Password1", new Role[0]);
        final MyUserMenu m2 = new MyUserMenu();
        assertNotNull(m2);
    }
}
