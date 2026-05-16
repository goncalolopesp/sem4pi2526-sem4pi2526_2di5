package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

@Entity
public class CertifiedEngine implements DomainEntity<Long> {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "engine_model_id", nullable = false)
    private EngineModel engineModel;

    @Version
    private Long version;

    protected CertifiedEngine() {
        // for ORM
    }

    public CertifiedEngine(final EngineModel engineModel) {
        Preconditions.nonNull(engineModel, "Engine Model cannot be null.");
        this.engineModel = engineModel;
    }
}