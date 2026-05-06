package pt.isep.sem4pi.infrastructure.bootstrap;

import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

// NOT IN USE, FOR NOW USE ../persistence/InMemoryRepositoryFactory.java

/**
 * Bootstrapper to register master users (admin, backoffice operator).
 */
public class MasterUsersBootstrapper {

    public boolean execute() {
        final UserRepository users = PersistenceContext.repositories().users();

        // ADMIN user
        final SystemUserBuilder adminBuilder = UserBuilderHelper.builder();
        adminBuilder.withUsername("admin").withPassword("Password1").withName("Admin", "User")
                .withEmail("admin@example.org").withRoles(SEM4PIRoles.ADMIN);
        final SystemUser admin = adminBuilder.build();
        users.save(admin);

        // BACKOFFICE_OPERATOR user
        final SystemUserBuilder boBuilder = UserBuilderHelper.builder();
        boBuilder.withUsername("backoffice").withPassword("Password1").withName("Back", "Office")
                .withEmail("backoffice@example.org").withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);
        final SystemUser boUser = boBuilder.build();
        users.save(boUser);

        //Add US056 bootstrappers

        new AircraftModelBootstrapper().execute();

        return true;
    }
}
