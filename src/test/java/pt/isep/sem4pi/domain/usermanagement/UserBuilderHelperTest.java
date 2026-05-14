package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Teste para o UserBuilderHelper.
 *
 * Garante que o builder devolve um SystemUserBuilder configurado com a política e encoder do projeto
 * e que é possível construir um SystemUser simples.
 */
public class UserBuilderHelperTest {

    /**
     * Verifica que o builder retorna um SystemUserBuilder e que build() produz um SystemUser.
     */
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
