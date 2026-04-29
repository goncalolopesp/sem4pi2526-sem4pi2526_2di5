package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserBuilderHelperTest {

    @Test
    public void builder_returnsSystemUserBuilder_and_builds() {
        final SystemUserBuilder b = UserBuilderHelper.builder();
        assertNotNull(b);
        b.withUsername("x").withPassword("Password1").withName("X","Y").withEmail("x@y.com").withRoles();
        final SystemUser u = b.build();
        assertNotNull(u);
        assertEquals("x", u.identity().toString());
    }
}
