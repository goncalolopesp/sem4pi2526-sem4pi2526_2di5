package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaID;

public interface AirControlAreaRepository extends DomainRepository<AirControlAreaID, AirControlArea> {

    default boolean containsOfIdentity(AirControlAreaID id) {
        return ofIdentity(id).isPresent();
    }

}