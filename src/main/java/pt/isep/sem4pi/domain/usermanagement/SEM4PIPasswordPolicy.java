package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.strings.util.StringPredicates;

/**
 * Politica de password da aplicacao SEM4PI.
 *
 * Define regras simples: minimo de 6 caracteres, obrigatorio um algarismo e uma letra maiuscula.
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
}
