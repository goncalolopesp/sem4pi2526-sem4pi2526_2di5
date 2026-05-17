package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Roles do projeto SEM4PI.
 *
 * Define constantes para as roles usadas na aplicacao e utilitarios para validacao.
 */
public final class SEM4PIRoles {

    private SEM4PIRoles() {
        // utility
    }

    public static final Role ADMIN = Role.valueOf("ADMIN");
    public static final Role BACKOFFICE_OPERATOR = Role.valueOf("BACKOFFICE_OPERATOR");
    public static final Role WEATHER_PERSON = Role.valueOf("WEATHER_PERSON");
    public static final Role PILOT = Role.valueOf("PILOT");
    public static final Role FLIGHT_CONTROL_OPERATOR = Role.valueOf("FLIGHT_CONTROL_OPERATOR");
    public static final Role AIR_TRANSPORT_COMPANY_COLLABORATOR = Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR");

    /**
     * Conjunto imutável de roles de colaborador (com acesso ao sistema de backoffice).
     */
    private static final Set<Role> COLLABORATOR_ROLES = Collections.unmodifiableSet(new LinkedHashSet<>(Set.of(
            ADMIN,
            BACKOFFICE_OPERATOR,
            WEATHER_PERSON,
            PILOT,
            FLIGHT_CONTROL_OPERATOR,
            AIR_TRANSPORT_COMPANY_COLLABORATOR
    )));

    /**
     * Roles que fazem sentido escolher ao criar utilizadores (valores nao "regular user").
     *
     * Nota: devolve uma cópia para evitar alterações externas.
     */
    public static Role[] nonUserValues() {
        return COLLABORATOR_ROLES.toArray(new Role[0]);
    }

    /**
     * Devolve o conjunto de roles colaboradoras.
     */
    public static Set<Role> collaboratorRoles() {
        return COLLABORATOR_ROLES;
    }

    /**
     * Verifica se a role e considerada colaboradora (tem poderes de backoffice/admin).
     */
    public static boolean isCollaborator(final Role role) {
        return role != null && COLLABORATOR_ROLES.contains(role);
    }
}
