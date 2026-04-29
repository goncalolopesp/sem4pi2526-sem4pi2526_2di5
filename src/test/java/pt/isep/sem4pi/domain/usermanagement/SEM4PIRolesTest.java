package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.Test;

import static org.junit.Assert.*;

public class SEM4PIRolesTest {

    @Test
    public void nonUserValues_notEmpty_and_isCollaborator() {
        final Role[] roles = SEM4PIRoles.nonUserValues();
        assertNotNull(roles);
        assertTrue(roles.length > 0);
        assertTrue(SEM4PIRoles.isCollaborator(SEM4PIRoles.ADMIN));
        assertFalse(SEM4PIRoles.isCollaborator(null));
    }
}

