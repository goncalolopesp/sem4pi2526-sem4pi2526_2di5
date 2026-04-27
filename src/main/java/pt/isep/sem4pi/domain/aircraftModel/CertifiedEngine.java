package pt.isep.sem4pi.domain.aircraftModel;

import jakarta.persistence.*;

@Entity
public class CertifiedEngine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the EngineModel Aggregate Root (by object or ID)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private EngineModel engineModel;

    protected CertifiedEngine() {
        // for ORM
    }

    public CertifiedEngine(final EngineModel engineModel) {
        if (engineModel == null) {
            throw new IllegalArgumentException("Engine Model cannot be null.");
        }
        this.engineModel = engineModel;
    }
}