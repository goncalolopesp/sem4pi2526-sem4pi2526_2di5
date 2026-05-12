package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Optional<BackofficeUser> findByEmail(final String email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email == null ? null : email.trim().toLowerCase());
        return matchOne("e.email = :email", params);
    }

    @Override
    public boolean existsByUsername(final String username) {
        return ofIdentity(username).isPresent();
    }

    @Override
    public boolean existsByEmail(final String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<BackofficeUser> findAll() {
        return (List<BackofficeUser>) match("1=1");
    }
}