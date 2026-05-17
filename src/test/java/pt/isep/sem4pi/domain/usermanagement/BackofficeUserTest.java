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
                Email.valueOf("joe@example.org"),
                PhoneNumber.valueOf("+351900000000"),
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
        assertEquals("joe", u.username());
        assertEquals("Joe Bloggs", u.name());
        assertEquals("joe@example.org", u.email().toString());
        assertEquals("+351900000000", u.phoneNumber().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalidInputs_throw() {
        new BackofficeUser("x", "", Email.valueOf("a@b.c"), PhoneNumber.valueOf("123"), LocalDate.now(), LocalDate.now());
    }
}
