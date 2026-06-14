package pt.isep.sem4pi;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.bootstrap.BackofficeConfigurationBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.MasterUsersBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airport.AirportBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.AirTransportCompanyBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.CompanyFleetBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.FlightRouteBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.PilotBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.weatherdata.WeatherBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirportRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaWeatherBatchRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessProtocol;
import pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessTcpServer;

public final class MainServer {

    private MainServer() {
        // launcher
    }

    public static void main(final String[] args) throws Exception {
        bootstrap();
        final int port = resolvePort(args);
        new RemoteAccessTcpServer(port).start();
    }

    static void bootstrap() {
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder()
        );

        new MasterUsersBootstrapper().execute();
        new BackofficeConfigurationBootstrapper().execute();
        new AirportBootstrapper(new JpaAirportRepository()).execute();
        new WeatherBootstrapper(new JpaWeatherBatchRepository()).execute();
        new AirTransportCompanyBootstrapper(new JpaAirTransportCompanyRepository()).execute();
        new FlightRouteBootstrapper().execute();
        new CompanyFleetBootstrapper().execute();
        new PilotBootstrapper().execute();
    }

    private static int resolvePort(final String[] args) {
        if (args != null && args.length > 0 && !args[0].isBlank()) {
            return Integer.parseInt(args[0].trim());
        }
        return RemoteAccessProtocol.resolvePort();
    }
}
