package pt.isep.sem4pi.domain.usermanagement;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testes para a política de password (domínio através do Authz framework).
 */
public class SEM4PIPasswordPolicyTest {

    @Test
    public void validPasswords_areSatisfied() {
        final var policy = new SEM4PIPasswordPolicy();
        assertTrue(policy.isSatisfiedBy("Password1"));
        assertTrue(policy.isSatisfiedBy("Abcdef1"));
    }

    @Test
    public void invalidPasswords_areNotSatisfied() {
        final var policy = new SEM4PIPasswordPolicy();
        assertFalse(policy.isSatisfiedBy(null));
        assertFalse(policy.isSatisfiedBy(""));
        assertFalse(policy.isSatisfiedBy("short"));
        assertFalse(policy.isSatisfiedBy("abcdef1")); // no capital
        assertFalse(policy.isSatisfiedBy("ABCDEF"));  // no digit
    }
}
