package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import pt.isep.sem4pi.application.controller.airtransportcompany.RegisterCustomerCollaboratorController;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class CustomerCollaboratorBootstrapper implements Action {

    @Override
    public boolean execute() {

        // Authenticate as admin so the controller's authz check passes
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        try {
            AirTransportCompanyRepository companyRepo = PersistenceContext.repositories().airTransportCompanies();
            AirTransportCompanyCollaboratorRepository collaboratorRepo = PersistenceContext.repositories().airTransportCompanyCollaborators();

            RegisterCustomerCollaboratorController ctrl = new RegisterCustomerCollaboratorController(companyRepo, collaboratorRepo);

            ctrl.registerCompanyCollaborator(
                    "tap_collab",
                    "Password123!",
                    "John",
                    "Doe",
                    "johndoe@personal.email.com",
                    new IcaoCode("TAP")
            );
            System.out.println("Air Transport Company Collaborator successfully bootstrapped.");
            return true;

        } catch (Exception e) {
            System.out.println("Failed to bootstrap collaborator: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            // Always clear the session, even if bootstrap fails
            if (AuthzRegistry.authorizationService().hasSession()) {
                AuthzRegistry.authorizationService().clearSession();
            }
        }
    }
}