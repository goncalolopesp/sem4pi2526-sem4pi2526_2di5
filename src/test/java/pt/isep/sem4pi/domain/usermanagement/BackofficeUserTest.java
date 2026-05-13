package pt.isep.sem4pi.domain.usermanagement;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Testes para a entidade BackofficeUser.
 *
 * Como o estado ativo/inativo passou para o SystemUser (framework), esta entidade
 * é apenas o perfil de negócio.
 */
public class BackofficeUserTest {

    @Test
    public void constructorAndGetters() {
        final BackofficeUser u = new BackofficeUser(
                "joe",
                "Joe Bloggs",
                "joe@example.org",
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
        assertEquals("joe", u.username());
        assertEquals("Joe Bloggs", u.name());
        assertEquals("joe@example.org", u.email());
        assertEquals("+351900000000", u.phoneNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalidInputs_throw() {
        new BackofficeUser(null, "", "a@b.c", "123", LocalDate.now(), LocalDate.now());
    }
}
