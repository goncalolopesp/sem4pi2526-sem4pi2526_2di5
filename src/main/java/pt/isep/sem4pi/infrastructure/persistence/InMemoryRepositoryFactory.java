package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;

import java.time.LocalDate;

/**
 * Fábrica de repositórios em memória.
 *
 * Usada como implementação padrão para persistência nas user stories US30-32.
 * Cria repositórios e semeia alguns utilizadores por omissão.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory {

    private static eapli.framework.infrastructure.authz.domain.repositories.UserRepository userRepository;
    private static BackofficeUserRepository backofficeUserRepository;

    @Override
    public TransactionalContext newTransactionalContext() {
        return null;
    }

    @Override
    public eapli.framework.infrastructure.authz.domain.repositories.UserRepository users(TransactionalContext tx) {
        return users();
    }

    @Override
    public eapli.framework.infrastructure.authz.domain.repositories.UserRepository users() {
        if (userRepository == null) {
            userRepository = new eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository();
            seedDefaultUsers(userRepository);
        }

        return userRepository;
    }

    @Override
    public BackofficeUserRepository backofficeUsers(TransactionalContext tx) {
        return backofficeUsers();
    }

    @Override
    public BackofficeUserRepository backofficeUsers() {
        if (backofficeUserRepository == null) {
            backofficeUserRepository = new InMemoryBackofficeUserRepository();
            seedDefaultBackofficeUsers(backofficeUserRepository);
        }
        return backofficeUserRepository;
    }

    private void seedDefaultUsers(final eapli.framework.infrastructure.authz.domain.repositories.UserRepository repo) {
        // ADMIN
        final SystemUserBuilder adminBuilder = UserBuilderHelper.builder();
        adminBuilder.withUsername("admin")
                .withPassword("Password1")
                .withName("Admin", "User")
                .withEmail("admin@example.org")
                .withRoles(SEM4PIRoles.ADMIN);

        repo.save(adminBuilder.build());

        // BACKOFFICE
        final SystemUserBuilder boBuilder = UserBuilderHelper.builder();
        boBuilder.withUsername("bo")
                .withPassword("Password1")
                .withName("Backoffice", "User")
                .withEmail("bo@example.org")
                .withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);

        repo.save(boBuilder.build());
    }

    private void seedDefaultBackofficeUsers(final BackofficeUserRepository repo) {
        if (!repo.existsByUsername("bo")) {
            final BackofficeUser bo = new BackofficeUser(
                    "bo",
                    "Backoffice User",
                    "bo@example.org",
                    "+351000000000",
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            );
            repo.save(bo);
        }
    }
}