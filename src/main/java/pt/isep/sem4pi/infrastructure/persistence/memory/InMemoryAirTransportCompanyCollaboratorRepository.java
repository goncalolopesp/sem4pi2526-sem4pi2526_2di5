package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAirTransportCompanyCollaboratorRepository
        extends InMemoryDomainRepository<AirTransportCompanyCollaborator, Long>
        implements AirTransportCompanyCollaboratorRepository {

    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public AirTransportCompanyCollaborator save(AirTransportCompanyCollaborator entity) {
        if (entity.identity() == null) {
            entity.assignIdentity(nextId.getAndIncrement());
        }
        return super.save(entity);
    }

    @Override
    public Optional<AirTransportCompanyCollaborator> findBySystemUser(SystemUser systemUser) {
        return matchOne(e -> e.systemUser().equals(systemUser));
    }

    @Override
    public Iterable<AirTransportCompanyCollaborator> findByCompany(AirTransportCompany company) {
        return match(e -> e.company().identityIcao().equals(company.identityIcao()));
    }
}