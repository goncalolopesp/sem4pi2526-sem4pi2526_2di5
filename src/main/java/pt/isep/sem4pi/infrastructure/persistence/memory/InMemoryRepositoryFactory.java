package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import pt.isep.sem4pi.domain.repositories.*;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.PhoneNumber;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.time.LocalDate;

/**
 * Fábrica de repositórios em memória.
 * <p>
 * Usada como implementação padrão para persistência nas user stories US30-32.
 * Cria repositórios e semeia alguns utilizadores por omissão.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory {

    private static eapli.framework.infrastructure.authz.domain.repositories.UserRepository userRepository;
    private static BackofficeUserRepository backofficeUserRepository;
    private static AircraftModelRepository aircraftModelRepository;
    private static EngineModelRepository engineModelRepository;
    private static ManufacturerRepository manufacturerRepository;
    private static AirControlAreaRepository airControlAreaRepository;
    private static AircraftRepository aircraftRepository;
    private static FlightRepository flightRepository;
    private static WeatherBatchRepository weatherBatchRepository;
    private static FlightRouteRepository flightRouteRepository;
    private static AirportRepository airportRepository;
    private static SimulationRepository simulationRepository;
    private static SimulationReportRepository simulationReportRepository;
    private static FlightPlanRepository flightPlanRepository;
    private static PilotRepository pilotRepository;

    @Override
    public TransactionalContext newTransactionalContext() {
        return new TransactionalContext() {
            @Override
            public void beginTransaction() {
                // no-op for in-memory repositories
            }

            @Override
            public void commit() {
                // no-op for in-memory repositories
            }

            @Override
            public void rollback() {
                // no-op for in-memory repositories
            }

            @Override
            public void close() {
                // no-op for in-memory repositories
            }

            @Override
            public boolean isActive() {
                return true;
            }
        };
    }

    @Override
    public UserRepository users(TransactionalContext tx) {
        return users();
    }

    @Override
    public UserRepository users() {
        if (userRepository == null) {
            userRepository = new InMemoryUserRepository();
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

    @Override
    public AirControlAreaRepository airControlAreas() {
        if (airControlAreaRepository == null) {
            return new InMemoryAirControlAreaRepository();
        }
        return airControlAreaRepository;
    }

    @Override
    public AirControlAreaRepository airControlAreas(TransactionalContext tx) {
        return airControlAreas();
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return new InMemoryAirTransportCompanyRepository();
    }

    @Override
    public AirTransportCompanyCollaboratorRepository airTransportCompanyCollaborators() {
        return new InMemoryAirTransportCompanyCollaboratorRepository();
    }

    @Override
    public AircraftRepository aircrafts() {
        if (aircraftRepository == null) {
            aircraftRepository = new InMemoryAircraftRepository();
        }
        return aircraftRepository;
    }

    @Override
    public FlightRepository flights() {
        if (flightRepository == null) {
            flightRepository = new InMemoryFlightRepository();
        }
        return flightRepository;
    }

    @Override
    public WeatherBatchRepository weatherBatches() {
        if (weatherBatchRepository == null) {
            weatherBatchRepository = new InMemoryWeatherBatchRepository();
        }
        return weatherBatchRepository;
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        if (flightRouteRepository == null) {
            flightRouteRepository = new InMemoryFlightRouteRepository();
        }
        return flightRouteRepository;
    }

    @Override
    public AirportRepository airports() {
        if (airportRepository == null) {
            airportRepository = new InMemoryAirportRepository();
        }
        return airportRepository;
    }

    @Override
    public SimulationRepository simulations() {
        if (simulationRepository == null) {
            simulationRepository = new InMemorySimulationRepository();
        }

        return simulationRepository;
    }

    @Override
    public SimulationRepository simulations(TransactionalContext tx) {
        return simulations();
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
        if (!repo.existsByUsername("admin")) {
            final BackofficeUser admin = new BackofficeUser(
                    "admin",
                    "Admin User",
                    Email.valueOf("admin@example.org"),
                    PhoneNumber.valueOf("+351000000001"),
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            );
            repo.save(admin);
        }

        if (!repo.existsByUsername("bo")) {
            final BackofficeUser bo = new BackofficeUser(
                    "bo",
                    "Backoffice User",
                    Email.valueOf("bo@example.org"),
                    PhoneNumber.valueOf("+351900000000"),
                    LocalDate.now().plusYears(1),
                    LocalDate.now()
            );
            repo.save(bo);
        }
    }

    @Override
    public SimulationReportRepository simulationReports() {
        if (simulationReportRepository == null) {
            simulationReportRepository = new InMemorySimulationReportRepository();
        }

        return simulationReportRepository;
    }

    @Override
    public SimulationReportRepository simulationReports(TransactionalContext tx) {
        return simulationReports();
    }

    @Override
    public FlightPlanRepository flightPlans() {
        if (flightPlanRepository == null) {
            flightPlanRepository = new InMemoryFlightPlanRepository();
        }
        return flightPlanRepository;
    }

    @Override
    public FlightPlanRepository flightPlans(TransactionalContext tx) {
        return flightPlans();
    }

    @Override
    public PilotRepository pilots() {
        if (pilotRepository == null) {
            pilotRepository = new InMemoryPilotRepository();
        }
        return pilotRepository;
    }

    @Override
    public PilotRepository pilots(TransactionalContext tx) {
        return pilots();
    }
}
