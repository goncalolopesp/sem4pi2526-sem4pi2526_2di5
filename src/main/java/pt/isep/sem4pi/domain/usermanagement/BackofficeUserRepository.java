package pt.isep.sem4pi.domain.usermanagement;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de perfis de utilizadores backoffice (US31/US32).
 *
 * Suporta consultas de unicidade por username/email e listagem para gestao de estado.
 */
public interface BackofficeUserRepository {

    BackofficeUser save(BackofficeUser user);

    Optional<BackofficeUser> ofIdentity(String username);

    Optional<BackofficeUser> findByEmail(Email email);

    boolean existsByUsername(String username);

    boolean existsByEmail(Email email);

    /**
     * Returns all backoffice users.
     */
    List<BackofficeUser> findAll();
}
