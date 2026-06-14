package pt.isep.sem4pi.infrastructure.bootstrap;

import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.PhoneNumber;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;

public class MasterUsersBootstrapper {

    public boolean execute() {
        final UserRepository users = PersistenceContext.repositories().users();
        final BackofficeUserRepository backofficeUsers = PersistenceContext.repositories().backofficeUsers();

        registerAdmin(users, backofficeUsers);
        registerBackoffice(users, backofficeUsers);
        registerWeatherPerson(users, backofficeUsers);
        registerPilot(users);

//        DEBUG
//        System.out.println("BOOTSTRAP USERS:");
//        System.out.println("admin exists? " + users.ofIdentity(
//                eapli.framework.infrastructure.authz.domain.model.Username.valueOf("admin")
//        ).isPresent());
//
//        System.out.println("backoffice exists? " + users.ofIdentity(
//                eapli.framework.infrastructure.authz.domain.model.Username.valueOf("backoffice")
//        ).isPresent());
//
//        System.out.println("admin backoffice user exists? " + backofficeUsers.existsByUsername("admin"));
//        System.out.println("backoffice backoffice user exists? " + backofficeUsers.existsByUsername("backoffice"));

        return true;
    }

    private void registerAdmin(final UserRepository users, final BackofficeUserRepository backofficeUsers) {
        if (users.ofIdentity(eapli.framework.infrastructure.authz.domain.model.Username.valueOf("admin")).isEmpty()) {
            final SystemUserBuilder builder = UserBuilderHelper.builder();
            builder.withUsername("admin")
                    .withPassword("Password1")
                    .withName("Admin", "User")
                    .withEmail("admin@example.org")
                    .withRoles(SEM4PIRoles.ADMIN);

            users.save(builder.build());
        }

        if (!backofficeUsers.existsByUsername("admin")) {
            backofficeUsers.save(new BackofficeUser(
                    "admin",
                    "Admin User",
                    Email.valueOf("admin@example.org"),
                    PhoneNumber.valueOf("+351000000001"),
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            ));
        }
    }

    private void registerBackoffice(final UserRepository users, final BackofficeUserRepository backofficeUsers) {
        if (users.ofIdentity(eapli.framework.infrastructure.authz.domain.model.Username.valueOf("backoffice")).isEmpty()) {
            final SystemUserBuilder builder = UserBuilderHelper.builder();
            builder.withUsername("backoffice")
                    .withPassword("Password1")
                    .withName("Backoffice", "User")
                    .withEmail("backoffice@example.org")
                    .withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);

            users.save(builder.build());
        }

        if (!backofficeUsers.existsByUsername("backoffice")) {
            backofficeUsers.save(new BackofficeUser(
                    "backoffice",
                    "Backoffice User",
                    Email.valueOf("backoffice@example.org"),
                    PhoneNumber.valueOf("+351000000000"),
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            ));
        }
    }

    private void registerWeatherPerson(final UserRepository users, final BackofficeUserRepository backofficeUsers) {
        if (users.ofIdentity(eapli.framework.infrastructure.authz.domain.model.Username.valueOf("weather")).isEmpty()) {
            final SystemUserBuilder builder = UserBuilderHelper.builder();
            builder.withUsername("weather")
                    .withPassword("Password1")
                    .withName("Weather", "Person")
                    .withEmail("weather@example.org")
                    .withRoles(SEM4PIRoles.WEATHER_PERSON);

            users.save(builder.build());
        }

        if (!backofficeUsers.existsByUsername("weather")) {
            backofficeUsers.save(new BackofficeUser(
                    "weather",
                    "Weather Person",
                    Email.valueOf("weather@example.org"),
                    PhoneNumber.valueOf("+351000000002"),
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            ));
        }
    }

    private void registerPilot(final UserRepository users) {
        if (users.ofIdentity(eapli.framework.infrastructure.authz.domain.model.Username.valueOf("pilot")).isEmpty()) {
            final SystemUserBuilder builder = UserBuilderHelper.builder();
            builder.withUsername("pilot")
                    .withPassword("Password1")
                    .withName("Pilot", "User")
                    .withEmail("pilot@example.org")
                    .withRoles(SEM4PIRoles.PILOT);

            users.save(builder.build());
        }
    }
}