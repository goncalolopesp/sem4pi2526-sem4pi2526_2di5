package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.repositories.PilotRepository;

import java.util.List;

public class JpaPilotRepository
        extends JpaAutoTxRepository<Pilot, PilotId, PilotId>
        implements PilotRepository {

    public JpaPilotRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "pilotId");
    }

    @Override
    public List<Pilot> findByCompany(final IataCodeCompany companyCode) {
        final TypedQuery<Pilot> query = entityManager().createQuery(
                "SELECT p FROM Pilot p WHERE p.companyCode.iataCode = :companyCode",
                Pilot.class);
        query.setParameter("companyCode", companyCode.code());
        return query.getResultList();
    }
}
