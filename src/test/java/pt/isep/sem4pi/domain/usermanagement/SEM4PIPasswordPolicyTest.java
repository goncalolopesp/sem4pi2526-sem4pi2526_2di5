package pt.isep.sem4pi.domain.usermanagement;

import org.junit.Test;

import static org.junit.Assert.*;

public class SEM4PIPasswordPolicyTest {

    private final SEM4PIPasswordPolicy policy = new SEM4PIPasswordPolicy();

    @Test
    public void validPassword_isSatisfiedAndStrength() {
        assertTrue(policy.isSatisfiedBy("GoodPass1"));
        assertEquals(SEM4PIPasswordPolicy.PasswordStrength.GOOD, policy.strength("GoodPass1"));
    }

    @Test
    public void weakPassword_fails() {
        assertFalse(policy.isSatisfiedBy("short"));
        assertEquals(SEM4PIPasswordPolicy.PasswordStrength.WEAK, policy.strength("short"));
    }

    @Test
    public void excellentPassword_strengthIsExcellent() {
        assertTrue(policy.isSatisfiedBy("LongerPass123"));
        assertEquals(SEM4PIPasswordPolicy.PasswordStrength.EXCELLENT, policy.strength("LongerPass123"));
    }
}

