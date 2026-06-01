package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaAirTransportCompanyCollaboratorRepository
        extends JpaAutoTxRepository<AirTransportCompanyCollaborator, Long, Long>
        implements AirTransportCompanyCollaboratorRepository {

    public JpaAirTransportCompanyCollaboratorRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaAirTransportCompanyCollaboratorRepository(final String puname) {
        super(puname, "id");
    }

    @Override
    public Optional<AirTransportCompanyCollaborator> findBySystemUser(SystemUser systemUser) {
        final Map<String, Object> params = new HashMap<>();
        params.put("systemUser", systemUser);
        return matchOne("e.systemUser = :systemUser", params);
    }

    @Override
    public Iterable<AirTransportCompanyCollaborator> findByCompany(AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        return match("e.company = :company", params);
    }
}