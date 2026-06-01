package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.PhoneNumber;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryBackofficeUserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RegisterBackofficeUserServiceTest {

    private static final class TestRepositoryFactory implements RepositoryFactory {
        private final UserRepository users;
        private final BackofficeUserRepository backofficeUsers;
        private final TransactionalContext tx;

        private TestRepositoryFactory(final UserRepository users, final BackofficeUserRepository backofficeUsers) {
            this.users = users;
            this.backofficeUsers = backofficeUsers;
            this.tx = Mockito.mock(TransactionalContext.class);
        }

        @Override
        public TransactionalContext newTransactionalContext() {
            return tx;
        }

        @Override
        public UserRepository users(final TransactionalContext tx) {
            return users;
        }

        @Override
        public UserRepository users() {
            return users;
        }

        @Override
        public BackofficeUserRepository backofficeUsers(final TransactionalContext tx) {
            return backofficeUsers;
        }

        @Override
        public BackofficeUserRepository backofficeUsers() {
            return backofficeUsers;
        }

        @Override public pt.isep.sem4pi.domain.repositories.AircraftModelRepository aircraftModels() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.EngineModelRepository engineModels() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.ManufacturerRepository manufacturers() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.AirControlAreaRepository airControlAreas() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.AirControlAreaRepository airControlAreas(final TransactionalContext tx) { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository airTransportCompanies() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository airTransportCompanyCollaborators() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.AircraftRepository aircrafts() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.FlightRepository flights() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.WeatherBatchRepository weatherBatches() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.AirportRepository airports() { return null; }
        @Override public pt.isep.sem4pi.domain.repositories.FlightRouteRepository flightRoutes() { return null; }
    }

    private TestRepositoryFactory factory;
    private RegisterBackofficeUserService service;

    @Before
    public void setUp() {
        factory = new TestRepositoryFactory(new InMemoryUserRepository(), new InMemoryBackofficeUserRepository());
        service = new RegisterBackofficeUserService(factory);
    }

    private static SystemUser buildSystemUser(final String username, final String email) {
        final SystemUserBuilder builder = UserBuilderHelper.builder();
        builder.withUsername(username)
                .withPassword("Password1")
                .withName("Test", "User")
                .withEmail(email)
                .withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);
        return builder.build();
    }

    @Test
    public void registerCreatesSystemUserAndBackofficeUser() {
        final String username = "newuser" + System.nanoTime();
        final String email = username + "@example.org";

        final BackofficeUser result = service.register(
                username,
                "Password1",
                "New",
                "User",
                email,
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );

        assertNotNull(result);
        assertTrue(factory.backofficeUsers().existsByUsername(username));
        assertTrue(factory.backofficeUsers().existsByEmail(Email.valueOf(email)));
        assertTrue(factory.users().ofIdentity(Username.valueOf(username)).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsDuplicateUsernameInBackofficeUsers() {
        final String username = "dupbo";
        factory.backofficeUsers().save(new BackofficeUser(
                username,
                "Dup Bo",
                Email.valueOf("dupbo@example.org"),
                PhoneNumber.valueOf("+351900000001"),
                LocalDate.now().plusYears(1),
                LocalDate.now()
        ));

        service.register(
                username,
                "Password1",
                "Duplicate",
                "Backoffice",
                "unique@example.org",
                "+351900000002",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsDuplicateUsernameInSystemUsers() {
        final String username = "dupsys";
        factory.users().save(buildSystemUser(username, "dupsys@example.org"));

        service.register(
                username,
                "Password1",
                "Duplicate",
                "System",
                "another@example.org",
                "+351900000003",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerRejectsDuplicateEmail() {
        factory.backofficeUsers().save(new BackofficeUser(
                "emailbo",
                "Email Bo",
                Email.valueOf("dupemail@example.org"),
                PhoneNumber.valueOf("+351900000004"),
                LocalDate.now().plusYears(1),
                LocalDate.now()
        ));

        service.register(
                "anotheruser",
                "Password1",
                "Another",
                "User",
                "dupemail@example.org",
                "+351900000005",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }

    @Test(expected = RuntimeException.class)
    public void registerPropagatesBackofficeSaveFailures() {
        final BackofficeUserRepository failingBo = Mockito.spy(new InMemoryBackofficeUserRepository());
        Mockito.doThrow(new RuntimeException("simulated failure")).when(failingBo).save(Mockito.any());

        final RegisterBackofficeUserService failingService = new RegisterBackofficeUserService(
                new TestRepositoryFactory(new InMemoryUserRepository(), failingBo)
        );

        failingService.register(
                "failbo",
                "Password1",
                "Fail",
                "Bo",
                "failbo@example.org",
                "+351900000006",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }

    @Test(expected = RuntimeException.class)
    public void registerPropagatesSystemUserSaveFailures() {
        final UserRepository failingUsers = Mockito.spy(new InMemoryUserRepository());
        Mockito.doThrow(new RuntimeException("simulated failure")).when(failingUsers).save(Mockito.any());

        final RegisterBackofficeUserService failingService = new RegisterBackofficeUserService(
                new TestRepositoryFactory(failingUsers, new InMemoryBackofficeUserRepository())
        );

        failingService.register(
                "failsys",
                "Password1",
                "Fail",
                "Sys",
                "failsys@example.org",
                "+351900000007",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );
    }

    @Test
    public void inMemoryTransactional_successfulRegistration() {
        final pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory memFactory = new pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory();
        final RegisterBackofficeUserService svc = new RegisterBackofficeUserService(memFactory);

        final String username = "txuser" + System.nanoTime();
        final String email = username + "@example.org";

        final BackofficeUser result = svc.register(
                username,
                "Password1",
                "New",
                "User",
                email,
                "+351900000000",
                LocalDate.now().plusYears(1),
                LocalDate.now()
        );

        assertNotNull(result);
        assertTrue(memFactory.backofficeUsers().existsByUsername(username));
        assertTrue(memFactory.users().ofIdentity(Username.valueOf(username)).isPresent());
    }

    @Test(expected = RuntimeException.class)
    public void inMemoryTransactional_backofficeSaveFailure_rollsBackSystemUser() {
        final pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory memFactory = new pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory();

        final eapli.framework.infrastructure.authz.domain.repositories.UserRepository userRepo = memFactory.users();
        final BackofficeUserRepository failingBo = Mockito.spy(new InMemoryBackofficeUserRepository());
        Mockito.doThrow(new RuntimeException("simulated failure")).when(failingBo).save(Mockito.any());

        final RepositoryFactory customFactory = new RepositoryFactory() {
            @Override
            public TransactionalContext newTransactionalContext() {
                return memFactory.newTransactionalContext();
            }

            @Override
            public UserRepository users(TransactionalContext tx) {
                return memFactory.users(tx);
            }

            @Override
            public UserRepository users() {
                return userRepo;
            }

            @Override
            public BackofficeUserRepository backofficeUsers(TransactionalContext tx) {
                return failingBo;
            }

            @Override
            public BackofficeUserRepository backofficeUsers() {
                return failingBo;
            }

            @Override public pt.isep.sem4pi.domain.repositories.AircraftModelRepository aircraftModels() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.EngineModelRepository engineModels() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.ManufacturerRepository manufacturers() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.AirControlAreaRepository airControlAreas() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.AirControlAreaRepository airControlAreas(final TransactionalContext tx) { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository airTransportCompanies() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository airTransportCompanyCollaborators() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.AircraftRepository aircrafts() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.FlightRepository flights() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.WeatherBatchRepository weatherBatches() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.AirportRepository airports() { return null; }
            @Override public pt.isep.sem4pi.domain.repositories.FlightRouteRepository flightRoutes() { return null; }
        };

        final RegisterBackofficeUserService svc = new RegisterBackofficeUserService(customFactory);

        final String username = "txfail" + System.nanoTime();
        final String email = username + "@example.org";

        try {
            svc.register(
                    username,
                    "Password1",
                    "Fail",
                    "Bo",
                    email,
                    "+351900000006",
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            );
        } finally {
            assertFalse(memFactory.users().ofIdentity(Username.valueOf(username)).isPresent());
            assertFalse(memFactory.backofficeUsers().existsByUsername(username));
        }
    }
}