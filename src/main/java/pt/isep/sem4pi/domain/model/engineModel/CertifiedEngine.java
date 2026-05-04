package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.domain.model.DomainEntity;
import jakarta.persistence.*;

@Entity
public class CertifiedEngine implements DomainEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk; // Chave primária gerada automaticamente para a base de dados

    // Esta é a referência segura ao trabalho do teu colega!
    @Embedded
    @AttributeOverride(name = "engineCode", column = @Column(name = "engine_model_id"))
    private EngineModelId engineModelId;

    protected CertifiedEngine() {
        // for ORM
    }

    public CertifiedEngine(EngineModelId engineModelId) {
        if (engineModelId == null) {
            throw new IllegalArgumentException("A Certified Engine must reference a valid EngineModelId.");
        }
        this.engineModelId = engineModelId;
    }

    public EngineModelId engineModelId() {
        return this.engineModelId;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof CertifiedEngine)) return false;
        final CertifiedEngine that = (CertifiedEngine) other;
        return this.engineModelId.equals(that.engineModelId());
    }

    @Override
    public Long identity() {
        return pk;
    }
}