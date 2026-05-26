package pt.isep.sem4pi.application.controller.airtransportcompany;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UseCaseController
public class ListCompanyFleetController {

    private final AuthorizationService authz;
    private final AirTransportCompanyCollaboratorRepository collaboratorRepository;

    public enum FleetSortOption {
        REGISTRATION,
        MAKER,
        MODEL,
        CAPACITY,
        AGE
    }

    public ListCompanyFleetController() {
        this.authz = AuthzRegistry.authorizationService();
        this.collaboratorRepository = PersistenceContext.repositories().airTransportCompanyCollaborators();
    }

    public ListCompanyFleetController(AuthorizationService authz, AirTransportCompanyCollaboratorRepository collaboratorRepository) {
        this.authz = authz;
        this.collaboratorRepository = collaboratorRepository;
    }

    public Iterable<Aircraft> listMyCompanyFleet(FleetSortOption sortOption) {
        authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, SEM4PIRoles.ADMIN);

        SystemUser currentUser = authz.session().get().authenticatedUser();

        AirTransportCompanyCollaborator collab = collaboratorRepository.findBySystemUser(currentUser)
                .orElseThrow(() -> new IllegalStateException("No collaborator profile found for the current user."));

        AirTransportCompany company = collab.company();

        // 1. Passar a frota (Iterable) para uma Lista para podermos ordenar
        List<Aircraft> fleetList = new ArrayList<>();
        company.getFleet().forEach(fleetList::add);

        // 2. Ordenar a lista consoante a opção (a, b, c, d) escolhida na UI
        switch (sortOption) {
            case REGISTRATION:
                fleetList.sort(Comparator.comparing(a -> a.identity().toString()));
                break;
            case MAKER:
                fleetList.sort(Comparator.comparing(a -> a.modelId().manufacturerId().toString()));
                break;
            case MODEL:
                fleetList.sort(Comparator.comparing(a -> a.modelId().modelName()));
                break;
            case CAPACITY:
                fleetList.sort(Comparator.comparingInt(a -> a.seats().getTotalCapacity()));
                break;
            case AGE:
                fleetList.sort(Comparator.comparingInt(Aircraft::age));
                break;
        }

        return fleetList;
    }
}