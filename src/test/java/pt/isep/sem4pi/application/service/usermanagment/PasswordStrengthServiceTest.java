package pt.isep.sem4pi.application.service.usermanagment;

import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordStrengthServiceTest {

    @Test
    public void strength_classifiesPasswords() {
        final var svc = new PasswordStrengthService();

        assertEquals(PasswordStrengthService.PasswordStrength.WEAK, svc.strength(null));
        assertEquals(PasswordStrengthService.PasswordStrength.WEAK, svc.strength("short"));
        assertEquals(PasswordStrengthService.PasswordStrength.GOOD, svc.strength("GoodPass1"));
        assertEquals(PasswordStrengthService.PasswordStrength.EXCELLENT, svc.strength("LongerPass123"));
    }
}

