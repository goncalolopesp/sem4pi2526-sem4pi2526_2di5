package pt.isep.sem4pi.domain.usermanagement;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Testes para a política de password (US31).
 *
 * Verifica regras básicas de validação e a avaliação da força da password.
 */
public class SEM4PIPasswordPolicyTest {

    private final SEM4PIPasswordPolicy policy = new SEM4PIPasswordPolicy();

    /**
     * Password válida deve satisfazer a política e ter força GOOD.
     */
    @Test
    public void validPassword_isSatisfiedAndStrength() {
        assertTrue(policy.isSatisfiedBy("GoodPass1"));
        assertEquals(SEM4PIPasswordPolicy.PasswordStrength.GOOD, policy.strength("GoodPass1"));
    }

    /**
     * Password fraca não satisfaz a política e a força é WEAK.
     */
    @Test
    public void weakPassword_fails() {
        assertFalse(policy.isSatisfiedBy("short"));
        assertEquals(SEM4PIPasswordPolicy.PasswordStrength.WEAK, policy.strength("short"));
    }

    /**
     * Password longa deve ser classificada como EXCELLENT.
     */
    @Test
    public void excellentPassword_strengthIsExcellent() {
        assertTrue(policy.isSatisfiedBy("LongerPass123"));
        assertEquals(SEM4PIPasswordPolicy.PasswordStrength.EXCELLENT, policy.strength("LongerPass123"));
    }
}
