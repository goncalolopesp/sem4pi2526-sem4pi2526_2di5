package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.Email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacao JPA do repositorio de BackofficeUser.
 *
 * Baseia-se no JpaAutoTxRepository e usa o campo "username" como chave.
 */
public class JpaBackofficeUserRepository
        extends JpaAutoTxRepository<BackofficeUser, String, String>
        implements BackofficeUserRepository {

    public JpaBackofficeUserRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "username");
    }

    @Override
    public BackofficeUser save(final BackofficeUser user) {
        return super.save(user);
    }

    @Override
    public Optional<BackofficeUser> ofIdentity(final String username) {
        return matchOne("e.username = :username", Map.of("username", username));
    }

    @Override
    public Optional<BackofficeUser> findByEmail(final Email email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email == null ? null : email.value());
        return matchOne("e.email.value = :email", params);
    }

    @Override
    public boolean existsByUsername(final String username) {
        return ofIdentity(username).isPresent();
    }

    @Override
    public boolean existsByEmail(final Email email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<BackofficeUser> findAll() {
        return (List<BackofficeUser>) match("1=1");
    }
}