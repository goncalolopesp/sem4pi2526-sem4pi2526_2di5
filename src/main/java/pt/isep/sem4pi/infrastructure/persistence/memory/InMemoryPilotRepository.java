package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.repositories.PilotRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryPilotRepository extends InMemoryDomainRepository<Pilot, PilotId>
        implements PilotRepository {

    @Override
    public List<Pilot> findByCompany(final IataCodeCompany companyCode) {
        final List<Pilot> result = new ArrayList<>();
        for (final Pilot pilot : findAll()) {
            if (pilot.companyCode().equals(companyCode)) {
                result.add(pilot);
            }
        }
        return result;
    }
}
