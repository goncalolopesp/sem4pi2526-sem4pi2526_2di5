package pt.isep.sem4pi.presentation.airtransportcompany;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCustomerCollaboratorsController;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCustomerCollaboratorsController.CollaboratorView;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.List;

public class ListCompanyCollaboratorsUI extends AbstractUI {

    private final ListCustomerCollaboratorsController theController;

    public ListCompanyCollaboratorsUI() {
        this.theController = new ListCustomerCollaboratorsController(
                PersistenceContext.repositories().airTransportCompanies(),
                PersistenceContext.repositories().airTransportCompanyCollaborators()
        );
    }

    @Override
    protected boolean doShow() {
        System.out.println("--- List Air Transport Company Collaborators ---");

        final String companyIcao = Console.readLine("Customer's Company ICAO Code (e.g., TAP): ");

        try {
            final List<CollaboratorView> collaborators =
                    theController.listCompanyCollaborators(new IcaoCodeCompany(companyIcao));

            if (collaborators.isEmpty()) {
                System.out.println("\nNo active collaborators found for this company.");
                return true;
            }

            System.out.printf("%n%-15s  %-15s  %-15s  %-30s%n", "Username", "First Name", "Last Name", "Email");
            for (final CollaboratorView c : collaborators) {
                System.out.printf("%-15s  %-15s  %-15s  %-30s%n",
                        c.username(), c.firstName(), c.lastName(), c.email());
            }
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] Invalid data: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR] Failed to list collaborators: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Company Collaborators";
    }
}