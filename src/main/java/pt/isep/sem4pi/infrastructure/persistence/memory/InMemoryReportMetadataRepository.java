package pt.isep.sem4pi.infrastructure.persistence.memory;

import pt.isep.sem4pi.domain.model.reporting.ReportMetadata;
import pt.isep.sem4pi.domain.repositories.ReportMetadataRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReportMetadataRepository implements ReportMetadataRepository {

    private final Map<String, ReportMetadata> metadataById = new ConcurrentHashMap<>();

    @Override
    public ReportMetadata save(final ReportMetadata metadata) {
        metadataById.put(metadata.reportId(), metadata);
        return metadata;
    }

    @Override
    public Optional<ReportMetadata> ofIdentity(final String reportId) {
        return Optional.ofNullable(metadataById.get(reportId));
    }

    @Override
    public List<ReportMetadata> findAll() {
        return List.copyOf(metadataById.values());
    }
}
