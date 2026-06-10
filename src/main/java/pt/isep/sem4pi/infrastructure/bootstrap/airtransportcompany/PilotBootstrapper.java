package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.repositories.PilotRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

public class PilotBootstrapper {

    private final PilotRepository pilotRepository;

    public PilotBootstrapper() {
        this.pilotRepository = PersistenceContext.repositories().pilots();
    }

    public void execute() {
        seedPilot("PILOT01", "TP", "pilot");
    }

    private void seedPilot(final String pilotCode, final String companyIata, final String ownerUsername) {
        final PilotId pilotId = new PilotId(pilotCode);
        final var existingPilot = pilotRepository.ofIdentity(pilotId);
        if (existingPilot.isPresent()) {
            existingPilot.get().assignOwner(ownerUsername);
            pilotRepository.save(existingPilot.get());
            return;
        }
        pilotRepository.save(new Pilot(pilotId, new IataCodeCompany(companyIata), ownerUsername));
    }
}
