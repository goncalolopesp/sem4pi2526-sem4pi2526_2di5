package pt.isep.sem4pi.presentation;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import pt.isep.sem4pi.infrastructure.bootstrap.MasterUsersBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airport.AirportBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.AirTransportCompanyBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany.FlightRouteBootstrapper;
import pt.isep.sem4pi.infrastructure.bootstrap.weatherdata.WeatherBootstrapper;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaAirportRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaFlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaWeatherBatchRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.bootstrap.BackofficeConfigurationBootstrapper;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.presentation.authz.LoginUI;

/**
 * Launcher da aplicacao Backoffice SEM4PI.
 *
 * Configura o servico de autenticacao e inicia o menu principal apos login.
 */
public final class SEM4PIBackoffice extends BaseApp {

    private SEM4PIBackoffice() {
        // prevent
    }

    public static void main(final String[] args) {
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder()
        );

//        Minimal Bootstrap
        new MasterUsersBootstrapper().execute();
        new BackofficeConfigurationBootstrapper().execute();
        new AirportBootstrapper(new JpaAirportRepository()).execute();
        new WeatherBootstrapper(new JpaWeatherBatchRepository()).execute();
        new AirTransportCompanyBootstrapper(new JpaAirTransportCompanyRepository()).execute();
        new FlightRouteBootstrapper().execute();

        new SEM4PIBackoffice().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        // login and go to main menu
        if (new LoginUI().show()) {
            final var menu = new MainMenu();
            menu.mainLoop();
        }
    }

    @Override
    protected String appTitle() {
        return "SEM4PI Back Office";
    }

    @Override
    protected String appGoodbye() {
        return "SEM4PI Back Office";
    }

    @Override
    protected void doSetupEventHandlers() {
        // no events
    }
}
