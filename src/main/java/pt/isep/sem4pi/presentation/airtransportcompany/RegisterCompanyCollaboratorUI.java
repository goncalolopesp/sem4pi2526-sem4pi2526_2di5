package pt.isep.sem4pi.presentation.airtransportcompany;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.airtransportcompany.RegisterCustomerCollaboratorController;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class RegisterCompanyCollaboratorUI extends AbstractUI {

    private final RegisterCustomerCollaboratorController theController;

    public RegisterCompanyCollaboratorUI(){
        this.theController = new RegisterCustomerCollaboratorController(
                PersistenceContext.repositories().airTransportCompanies(),
                PersistenceContext.repositories().airTransportCompanyCollaborators()
        );
    }

    @Override
    protected boolean doShow() {
        System.out.println("--- Register Air Transport Company Collaborator ---");

        final String companyIcao = Console.readLine("Customer's Company ICAO Code (e.g., TAP): ");
        final String username = Console.readLine("Collaborator Username: ");
        final String password = Console.readLine("Password: ");
        final String firstName = Console.readLine("First Name: ");
        final String lastName = Console.readLine("Last Name: ");
        final String email = Console.readLine("Email: ");

        try {
            this.theController.registerCompanyCollaborator(username, password, firstName, lastName, email, new IcaoCode(companyIcao));
            System.out.println("\n[SUCCESS] Company Collaborator successfully registered!");
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] Invalid data: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR] Failed to register collaborator: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register a Company Collaborator";
    }
}