package pt.isep.sem4pi.presentation;

import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.ExitWithMessageAction;
import eapli.framework.presentation.console.ShowMessageAction;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;
import pt.isep.sem4pi.presentation.AddAirport.AddAirportUI;
import pt.isep.sem4pi.presentation.aircraft.AddAircraftUI;
import pt.isep.sem4pi.presentation.aircraft.RetireAircraftAction;
import pt.isep.sem4pi.presentation.aircraftModel.AddAircraftModelAction;
import pt.isep.sem4pi.presentation.aircraftModel.CertifyEngineModelAction;
import pt.isep.sem4pi.presentation.airtransportcompany.*;
import pt.isep.sem4pi.presentation.authz.ListUsersAction;
import pt.isep.sem4pi.presentation.usermanagement.ManageBackofficeUserStatusUI;
import pt.isep.sem4pi.presentation.engineModel.RegisterEngineModelAction;
import pt.isep.sem4pi.presentation.aircontrolarea.RegisterAirControlAreaAction;
import pt.isep.sem4pi.presentation.weatherdata.RegisterWeatherUI;
import pt.isep.sem4pi.presentation.weatherdata.ImportWeatherDataUI;
import pt.isep.sem4pi.presentation.weatherdata.ConsultWeatherDataUI;

/**
 * Menu principal da aplicacao SEM4PI.
 *
 * Mostra opcoes com base nas roles do utilizador autenticado.
 */
public class MainMenu extends AbstractUI {

    private static final String RETURN_LABEL = "Return ";

    private static final int EXIT_OPTION = 0;

    // USERS
    private static final int ADD_USER_OPTION = 1;
    private static final int LIST_USERS_OPTION = 2;
    private static final int DEACTIVATE_USER_OPTION = 3;

    // BACKOFFICE CONFIGURATION
    private static final int REGISTER_ENGINE_MODEL_OPTION = 1;
    private static final int REGISTER_AIRCRAFT_MODEL_OPTION = 2;
    private static final int CERTIFY_ENGINE_MODEL_OPTION = 3;

    // AirTransportCompany
    private static final int REGISTER_AIR_TRANSPORT_COMPANY_OPTION = 1;
    private static final int REGISTER_ATCC_COLLABORATOR = 2;
    private static final int LIST_ATCC_COLABORATORS = 3;
    private static final int RETIRE_AIRCRAFT_OPTION = 4;
    private static final int ADD_AIRCRAFT_OPTION = 5;
    private static final int LIST_ATCC_FLEET = 6;

    // Airport
    private static final int ADD_AIRPORT_OPTIONS = 1;

    // Weather Data
    private static final int REGISTER_WEATHER_DATA_OPTIONS = 1;
    private static final int IMPORT_WEATHER_DATA_OPTIONS = 2;
    private static final int CONSULT_WEATHER_DATA_OPTIONS = 3;

    //Air Control Area
    private static final int ADD_AIR_CONTROL_AREA_OPTIONS = 1;

    // MAIN MENU
    private static final int MY_USER_OPTION = 1;
    private static final int ADMIN_OPTION = 2;
    private static final int USERS_OPTION = 3;
    private static final int BACKOFFICE_CONFIGURATION_OPTION = 4;
    private static final int AIR_TRANSPORT_COMPANY_OPTIONS = 5;
    private static final int AIRPORT_OPTIONS = 6;
    private static final int AIR_CONTROL_AREA_OPTIONS = 7;
    private static final int WEATHER_DATA_OPTIONS = 8;

    private static final String SEPARATOR_LABEL = "--------------";

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    @Override
    public boolean show() {
        drawFormTitle();
        return doShow();
    }

    @Override
    public boolean doShow() {
        final var menu = buildMainMenu();
        final MenuRenderer renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }

    @Override
    public String headline() {
        return authz.session().map(s -> "SEM4PI [ @" + s.authenticatedUser().identity() + " ]").orElse("SEM4PI [ ==Anonymous== ]");
    }

    /**
     * Executa o ciclo do menu principal ate o utilizador escolher sair.
     */
    public void mainLoop() {
        var exit = false;
        while (!exit) {
            exit = this.show();
        }
    }

    private Menu buildMainMenu() {
        final var mainMenu = new Menu();

        final var myUserMenu = new pt.isep.sem4pi.presentation.authz.MyUserMenu();
        mainMenu.addSubMenu(MY_USER_OPTION, myUserMenu);

        mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));

        if (authz.hasSession()) {

            final boolean isBackoffice =
                    authz.isAuthenticatedUserAuthorizedTo(
                            SEM4PIRoles.BACKOFFICE_OPERATOR,
                            SEM4PIRoles.ADMIN);

            final boolean isAdmin =
                    authz.isAuthenticatedUserAuthorizedTo(
                            SEM4PIRoles.ADMIN);

            final boolean isAtcc =
                    authz.isAuthenticatedUserAuthorizedTo(
                            SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                            SEM4PIRoles.ADMIN);

            final boolean isWeatherManager =
                    authz.isAuthenticatedUserAuthorizedTo(
                            SEM4PIRoles.ADMIN,
                            SEM4PIRoles.WEATHER_PERSON);

            final boolean isWeatherConsultant =
                    authz.isAuthenticatedUserAuthorizedTo(
                            SEM4PIRoles.ADMIN,
                            SEM4PIRoles.WEATHER_PERSON,
                            SEM4PIRoles.PILOT,
                            SEM4PIRoles.FLIGHT_CONTROL_OPERATOR);

            if (isAdmin) {
                final var adminMenu = buildAdminMenu();
                mainMenu.addSubMenu(ADMIN_OPTION, adminMenu);
            }

            if (isBackoffice) {
                mainMenu.addSubMenu(USERS_OPTION, buildUsersMenu());
                mainMenu.addSubMenu(BACKOFFICE_CONFIGURATION_OPTION, buildBackofficeConfigurationMenu());
                mainMenu.addSubMenu(AIR_TRANSPORT_COMPANY_OPTIONS, buildAirTransportCompanyMenu());
                mainMenu.addSubMenu(AIRPORT_OPTIONS, buildAirportMenu());
                mainMenu.addSubMenu(AIR_CONTROL_AREA_OPTIONS, buildAirControlAreaMenu());
            } else if (isAtcc) {
                mainMenu.addSubMenu(AIR_TRANSPORT_COMPANY_OPTIONS, buildAirTransportCompanyMenu());
            }

            if (isWeatherManager || isWeatherConsultant) {
                mainMenu.addSubMenu(WEATHER_DATA_OPTIONS, buildWeatherDataMenu(isWeatherManager, isWeatherConsultant));
            }
        }

        mainMenu.addItem(EXIT_OPTION, "Exit", new ExitWithMessageAction("Bye, Bye"));

        return mainMenu;
    }

    private Menu buildAdminMenu() {
        final var menu = new Menu("Admin >");

        menu.addItem(1, "Register Backoffice User",
                new pt.isep.sem4pi.presentation.usermanagement.RegisterBackofficeUserUI()::show);

        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

    private Menu buildUsersMenu() {
        final var menu = new Menu("Users >");

        menu.addItem(ADD_USER_OPTION, "Add User", new ShowMessageAction("Add User - TODO"));
        menu.addItem(LIST_USERS_OPTION, "List Users", new ListUsersAction());
        menu.addItem(DEACTIVATE_USER_OPTION, "Manage User status", new ManageBackofficeUserStatusUI()::show);
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

    private Menu buildBackofficeConfigurationMenu() {
        final var menu = new Menu("Backoffice Configuration >");
        menu.addItem(REGISTER_ENGINE_MODEL_OPTION, "Register Engine Model", new RegisterEngineModelAction());
        menu.addItem(REGISTER_AIRCRAFT_MODEL_OPTION, "Add Aircraft Model", new AddAircraftModelAction());
        menu.addItem(CERTIFY_ENGINE_MODEL_OPTION, "Certify Engine Model", new CertifyEngineModelAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildAirTransportCompanyMenu()
    {
        final var menu = new Menu("AirTransportCompany >");

        menu.addItem(REGISTER_AIR_TRANSPORT_COMPANY_OPTION, "Register Air Transport Company", new RegisterAirTransportCompanyUI()::show);
        menu.addItem(REGISTER_ATCC_COLLABORATOR, "Register Company Collaborator", new RegisterCompanyCollaboratorAction());
        menu.addItem(LIST_ATCC_COLABORATORS, "List Company Collaborators", new ListCompanyCollaboratorsAction());
        menu.addItem(RETIRE_AIRCRAFT_OPTION, "Retire Aircraft", new RetireAircraftAction());
        menu.addItem(ADD_AIRCRAFT_OPTION, "Add Aircraft", new AddAircraftUI()::show);
        menu.addItem(LIST_ATCC_FLEET, "List Company Fleet", new ListCompanyFleetAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

    private Menu buildAirportMenu()
    {
        final var menu = new Menu("Airport >");

        menu.addItem(ADD_AIRPORT_OPTIONS, "Add Airport", new AddAirportUI()::show);
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

    private Menu buildAirControlAreaMenu(){
        final var menu = new Menu("Air Control Area >");

        menu.addItem(ADD_AIR_CONTROL_AREA_OPTIONS, "Register Air Control Area", new RegisterAirControlAreaAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

    private Menu buildWeatherDataMenu(boolean canManage, boolean canConsult) {
        final var menu = new Menu("Weather Data >");

        if (canManage) {
            menu.addItem(REGISTER_WEATHER_DATA_OPTIONS, "Register Weather Data", new RegisterWeatherUI()::show);
            menu.addItem(IMPORT_WEATHER_DATA_OPTIONS, "Import Weather Data", new ImportWeatherDataUI()::show);
        }
        if (canConsult) {
            menu.addItem(CONSULT_WEATHER_DATA_OPTIONS, "Consult Weather Data", new ConsultWeatherDataUI()::show);
        }
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

}
