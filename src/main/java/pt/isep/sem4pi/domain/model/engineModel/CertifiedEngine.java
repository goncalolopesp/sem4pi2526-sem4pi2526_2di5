package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;

@Entity
public class CertifiedEngine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "engine_model_id"))
    private EngineModelId engineModelId;

    protected CertifiedEngine() {
    }

    public CertifiedEngine(EngineModelId engineModelId) {
        Preconditions.nonNull(engineModelId, "EngineModelId não pode ser nulo.");
        this.engineModelId = engineModelId;
    }

    public EngineModelId engineModelId() {
        return this.engineModelId;
    }
}