package pt.isep.sem4pi.domain.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.Role;

/**
 * Roles do projeto SEM4PI.
 *
 * Define constantes para as roles usadas na aplicacao e utilitarios para validacao.
 */
public final class SEM4PIRoles {

    public static final Role ADMIN = Role.valueOf("ADMIN");
    public static final Role BACKOFFICE_OPERATOR = Role.valueOf("BACKOFFICE_OPERATOR");
    public static final Role WEATHER_PERSON = Role.valueOf("WEATHER_PERSON");
    public static final Role PILOT = Role.valueOf("PILOT");
    public static final Role FLIGHT_CONTROL_OPERATOR = Role.valueOf("FLIGHT_CONTROL_OPERATOR");
    public static final Role AIR_TRANSPORT_COMPANY_COLLABORATOR = Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR");

    /**
     * Roles que fazem sentido escolher ao criar utilizadores (valores nao "regular user").
     * @return array de roles
     */
    public static Role[] nonUserValues() {
        return new Role[] { ADMIN, BACKOFFICE_OPERATOR, WEATHER_PERSON, PILOT, FLIGHT_CONTROL_OPERATOR,
                AIR_TRANSPORT_COMPANY_COLLABORATOR };
    }

    /**
     * Verifica se a role e considerada colaboradora (tem poderes de backoffice/admin).
     * @param role role a testar
     * @return true se for uma role colaboradora
     */
    public static boolean isCollaborator(final Role role) {
        if (role == null) {
            return false;
        }
        return role.equals(ADMIN) || role.equals(BACKOFFICE_OPERATOR) || role.equals(WEATHER_PERSON)
                || role.equals(PILOT) || role.equals(FLIGHT_CONTROL_OPERATOR)
                || role.equals(AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }
}
