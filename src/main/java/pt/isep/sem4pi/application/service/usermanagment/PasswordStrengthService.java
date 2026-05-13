package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.strings.util.StringPredicates;

/**
 * Serviço aplicacional para avaliar a "força" de uma password.
 *
 * Nota: isto é uma regra de UX (feedback ao utilizador) e não faz parte da política
 * formal de validação (PasswordPolicy), por isso vive fora do domínio.
 */
public class PasswordStrengthService {

    public enum PasswordStrength {
        WEAK, GOOD, EXCELLENT
    }

    public PasswordStrength strength(final String rawPassword) {
        if (StringPredicates.isNullOrEmpty(rawPassword)) {
            return PasswordStrength.WEAK;
        }

        if (rawPassword.length() >= 12
                || (rawPassword.length() >= 8 && StringPredicates.containsAny(rawPassword, "$#!%&?"))) {
            return PasswordStrength.EXCELLENT;
        }

        if (rawPassword.length() >= 8) {
            return PasswordStrength.GOOD;
        }

        return PasswordStrength.WEAK;
    }
}

