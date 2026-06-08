package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;

import java.util.List;

public interface PilotRepository extends DomainRepository<PilotId, Pilot> {

    List<Pilot> findByCompany(IataCodeCompany companyCode);
}
