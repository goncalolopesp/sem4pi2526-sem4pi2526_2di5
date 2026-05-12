package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftModelRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryEngineModelRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryManufacturerRepository;

public class InMemoryRepositoryFactory implements RepositoryFactory {

    private static eapli.framework.infrastructure.authz.domain.repositories.UserRepository userRepository;
    private static AircraftModelRepository aircraftModelRepository;
    private static EngineModelRepository engineModelRepository;
    private static ManufacturerRepository manufacturerRepository;

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
    public AircraftModelRepository aircraftModels() {
        if (aircraftModelRepository == null) {
            aircraftModelRepository = new InMemoryAircraftModelRepository();
        }
        return aircraftModelRepository;
    }

    @Override
    public EngineModelRepository engineModels() {
        if (engineModelRepository == null) {
            engineModelRepository = new InMemoryEngineModelRepository();
        }
        return engineModelRepository;
    }

    @Override
    public ManufacturerRepository manufacturers() {
        if (manufacturerRepository == null) {
            manufacturerRepository = new InMemoryManufacturerRepository();
        }
        return manufacturerRepository;
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
}
