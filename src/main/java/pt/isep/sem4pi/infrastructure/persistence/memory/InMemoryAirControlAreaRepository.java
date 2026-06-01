package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;

public class InMemoryAirControlAreaRepository
        extends InMemoryDomainRepository<AirControlArea, AirControlAreaID>
        implements AirControlAreaRepository {

    public InMemoryAirControlAreaRepository() {
        super(AirControlArea::identity);
    }
}