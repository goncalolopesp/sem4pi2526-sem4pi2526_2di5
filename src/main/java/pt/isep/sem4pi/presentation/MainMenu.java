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
import pt.isep.sem4pi.presentation.airtransportcompany.RegisterAirTransportCompanyUI;
import pt.isep.sem4pi.presentation.usermanagement.ListBackofficeUsersUI;
import pt.isep.sem4pi.presentation.usermanagement.ManageBackofficeUserStatusUI;

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

    // AirTransportCompany
    private static final int REGISTER_AIR_TRANSPORT_COMPANY_OPTION = 1;

    // MAIN MENU
    private static final int MY_USER_OPTION = 1;
    private static final int USERS_OPTION = 2;
    private static final int AIR_TRANSPORT_COMPANY_OPTIONS = 3;

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
            if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN)) {
                final var usersMenu = buildUsersMenu();
                final var airTransportCompanyMenu = buildAirTransportCompanyMenu();

                mainMenu.addSubMenu(USERS_OPTION, usersMenu);
                mainMenu.addSubMenu(AIR_TRANSPORT_COMPANY_OPTIONS, airTransportCompanyMenu);
            }

            if (authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
                final var adminMenu = new Menu("Admin >");
                adminMenu.addItem(1, "Admin Task (TODO)", new ShowMessageAction("Admin task not implemented yet"));
                adminMenu.addItem(2, "Register Backoffice User", new pt.isep.sem4pi.presentation.usermanagement.RegisterBackofficeUserUI()::show);
                adminMenu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
                mainMenu.addSubMenu(3, adminMenu);
            }
        }

        mainMenu.addItem(EXIT_OPTION, "Exit", new ExitWithMessageAction("Bye, Bye"));

        return mainMenu;
    }

    private Menu buildUsersMenu() {
        final var menu = new Menu("Users >");

        menu.addItem(ADD_USER_OPTION, "Add User", new ShowMessageAction("Add User - TODO"));
        menu.addItem(LIST_USERS_OPTION, "List all Users", new ListBackofficeUsersUI()::show);
        menu.addItem(DEACTIVATE_USER_OPTION, "Manage User status", new ManageBackofficeUserStatusUI()::show);
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }

    private Menu buildAirTransportCompanyMenu()
    {
        final var menu = new Menu("AirTransportCompany >");

        menu.addItem(REGISTER_AIR_TRANSPORT_COMPANY_OPTION, "Register Air Trasnport Company", new RegisterAirTransportCompanyUI()::show);
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);

        return menu;
    }
}
