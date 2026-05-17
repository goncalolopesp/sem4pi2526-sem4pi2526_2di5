package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import eapli.framework.actions.Action;
import pt.isep.sem4pi.application.controller.airtransportcompany.RegisterCustomerCollaboratorController;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class CustomerCollaboratorBootstrapper implements Action {

    @Override
    public boolean execute() {

        AirTransportCompanyRepository companyRepo = PersistenceContext.repositories().airTransportCompanies();
        AirTransportCompanyCollaboratorRepository collaboratorRepo = PersistenceContext.repositories().airTransportCompanyCollaborators();

        RegisterCustomerCollaboratorController ctrl = new RegisterCustomerCollaboratorController(companyRepo, collaboratorRepo);

        try {
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
            return false;
        }
    }
}