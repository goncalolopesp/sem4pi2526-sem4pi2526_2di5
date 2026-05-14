package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.Email;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repositório em memória para BackofficeUser.
 *
 * Implementa operações básicas usadas nas user stories de gestão de utilizadores.
 * Não usa base de dados; adequado para testes e para a implementação padrão deste projecto.
 */
public class InMemoryBackofficeUserRepository implements BackofficeUserRepository {

    private final Map<String, BackofficeUser> byUsername = new ConcurrentHashMap<>();
    private final Map<String, BackofficeUser> byEmail = new ConcurrentHashMap<>();

    @Override
    public BackofficeUser save(final BackofficeUser user) {
        byUsername.put(user.username(), user);
        byEmail.put(user.email().toString(), user);
        return user;
    }

    @Override
    public Optional<BackofficeUser> ofIdentity(final String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    @Override
    public Optional<BackofficeUser> findByEmail(final Email email) {
        return Optional.ofNullable(byEmail.get(email == null ? null : email.toString()));
    }

    @Override
    public boolean existsByUsername(final String username) {
        return byUsername.containsKey(username);
    }

    @Override
    public boolean existsByEmail(final Email email) {
        return byEmail.containsKey(email == null ? null : email.toString());
    }

    @Override
    public List<BackofficeUser> findAll() {
        return byUsername.values().stream().collect(Collectors.toList());
    }
}
