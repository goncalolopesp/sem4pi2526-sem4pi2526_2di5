package pt.isep.sem4pi.application.controller.airtransportcompany;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCodeCompany;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.util.ArrayList;
import java.util.List;

public class ListCustomerCollaboratorsController {

    public record CollaboratorView(String username, String firstName, String lastName, String email) {}

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AirTransportCompanyRepository companyRepo;
    private final AirTransportCompanyCollaboratorRepository collaboratorRepo;

    public ListCustomerCollaboratorsController(AirTransportCompanyRepository companyRepo,
                                               AirTransportCompanyCollaboratorRepository collaboratorRepo) {
        this.companyRepo = companyRepo;
        this.collaboratorRepo = collaboratorRepo;
    }

    public List<CollaboratorView> listCompanyCollaborators(IcaoCodeCompany companyCode) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR);

        AirTransportCompany company = companyRepo.findByIcaoCode(companyCode)
                .orElseThrow(() -> new IllegalArgumentException("Air Transport Company not found."));

        List<CollaboratorView> result = new ArrayList<>();
        for (AirTransportCompanyCollaborator collaborator : collaboratorRepo.findByCompany(company)) {
            SystemUser user = collaborator.systemUser();
            if (!user.isActive()) {
                continue;
            }
            result.add(new CollaboratorView(
                    user.identity().toString(),
                    user.name().firstName(),
                    user.name().lastName(),
                    user.email().toString()
            ));
        }
        return result;
    }
}