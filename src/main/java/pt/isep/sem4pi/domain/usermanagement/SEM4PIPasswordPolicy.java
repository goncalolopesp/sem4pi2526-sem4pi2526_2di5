package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.strings.util.StringPredicates;

/**
 * Password policy for the SEM4PI application.
 */
public class SEM4PIPasswordPolicy implements PasswordPolicy {

    @Override
    public boolean isSatisfiedBy(final String rawPassword) {
        // sanity checks
        if (StringPredicates.isNullOrEmpty(rawPassword)) {
            return false;
        }

        // at least 6 characters long and must contain a digit
        if (rawPassword.length() < 6 || !StringPredicates.containsDigit(rawPassword)) {
            return false;
        }

        // at least one capital letter
        return StringPredicates.containsCapital(rawPassword);
    }

    public enum PasswordStrength {
        WEAK, GOOD, EXCELLENT
    }

    public PasswordStrength strength(final String rawPassword) {
        if (StringPredicates.isNullOrEmpty(rawPassword)) {
            return PasswordStrength.WEAK;
        }

        if (rawPassword.length() >= 12 || (rawPassword.length() >= 8 && StringPredicates.containsAny(rawPassword, "$#!%&?"))) {
            return PasswordStrength.EXCELLENT;
        } else if (rawPassword.length() >= 8) {
            return PasswordStrength.GOOD;
        }
        return PasswordStrength.WEAK;
    }
}

