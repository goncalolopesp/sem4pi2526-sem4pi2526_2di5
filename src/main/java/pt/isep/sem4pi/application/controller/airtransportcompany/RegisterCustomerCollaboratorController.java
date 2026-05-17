package pt.isep.sem4pi.application.controller.airtransportcompany;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.util.Set;

public class RegisterCustomerCollaboratorController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final UserManagementService userSvc = AuthzRegistry.userService();
    private final AirTransportCompanyRepository companyRepo;
    private final AirTransportCompanyCollaboratorRepository collaboratorRepo;

    public RegisterCustomerCollaboratorController(AirTransportCompanyRepository companyRepo, AirTransportCompanyCollaboratorRepository collaboratorRepo) {
        this.companyRepo = companyRepo;
        this.collaboratorRepo = collaboratorRepo;
    }

    public AirTransportCompanyCollaborator registerCompanyCollaborator(
            String username, String password, String firstName, String lastName, String email, IcaoCode companyCode) {

        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR);

        AirTransportCompany company = companyRepo.findByIcaoCode(companyCode)
                .orElseThrow(() -> new IllegalArgumentException("Air Transport Company not found."));

        SystemUser newUser = userSvc.registerNewUser(
                username, password, firstName, lastName, email,
                Set.of(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
        );

        AirTransportCompanyCollaborator collaborator = new AirTransportCompanyCollaborator(newUser, company);
        return collaboratorRepo.save(collaborator);
    }
}