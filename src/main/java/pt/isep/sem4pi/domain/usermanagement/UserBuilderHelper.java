package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.util.Utility;

/**
 * Helper para construir SystemUser usando as definições de senha e codificação do projeto.
 *
 * Usado quando é preciso criar um SystemUser com a política local e o encoder definido.
 */
@Utility
public final class UserBuilderHelper {

    private UserBuilderHelper() {
        // utility
    }

    /**
     * Retorna um SystemUserBuilder configurado com a política SEM4PI e codificador em claro.
     * @return SystemUserBuilder pronto a usar
     */
    public static SystemUserBuilder builder() {
        return new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder());
    }
}
