package pt.isep.sem4pi.domain.model.engineModel;

import eapli.framework.representations.RepresentationBuilder;
import eapli.framework.representations.Representationable;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class CertifiedEngine implements Representationable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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

    public boolean hasEngineModel(final EngineModel engineModel) {
        if (engineModel == null) {
            return false;
        }
        return this.engineModel.identity().equals(engineModel.identity());
    }

    @Override
    public <R> R buildRepresentation(RepresentationBuilder<R> builder) {
        builder.startObject("certifiedEngine")
                .withProperty("engineModelId", this.engineModel.identity().toString())
                .endObject();

        return null;
    }
}