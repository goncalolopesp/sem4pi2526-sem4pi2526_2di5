package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;

import java.util.Optional;

public interface AirTransportCompanyCollaboratorRepository extends DomainRepository<Long, AirTransportCompanyCollaborator> {

    Optional<AirTransportCompanyCollaborator> findBySystemUser(SystemUser systemUser);

    Iterable<AirTransportCompanyCollaborator> findByCompany(AirTransportCompany company);
}