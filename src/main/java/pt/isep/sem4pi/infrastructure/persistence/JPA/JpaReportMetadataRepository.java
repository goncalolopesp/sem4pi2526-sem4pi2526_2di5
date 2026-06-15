package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.model.reporting.ReportMetadata;
import pt.isep.sem4pi.domain.repositories.ReportMetadataRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaReportMetadataRepository
        extends JpaAutoTxRepository<ReportMetadata, String, String>
        implements ReportMetadataRepository {

    public JpaReportMetadataRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "reportId");
    }

    public JpaReportMetadataRepository(final TransactionalContext tx) {
        super(tx, "reportId");
    }

    @Override
    public ReportMetadata save(final ReportMetadata metadata) {
        return super.save(metadata);
    }

    @Override
    public Optional<ReportMetadata> ofIdentity(final String reportId) {
        return matchOne("e.reportId = :reportId", Map.of("reportId", reportId));
    }

    @Override
    public List<ReportMetadata> findAll() {
        return (List<ReportMetadata>) match("1=1");
    }
}
