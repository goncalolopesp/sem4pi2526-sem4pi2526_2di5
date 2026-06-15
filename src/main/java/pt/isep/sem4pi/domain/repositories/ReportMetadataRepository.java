package pt.isep.sem4pi.domain.repositories;

import pt.isep.sem4pi.domain.model.reporting.ReportMetadata;

import java.util.List;
import java.util.Optional;

public interface ReportMetadataRepository {

    // DEFESA:
    // A interface mantém o domínio independente de JPA e permite implementações em memória nos testes.
    ReportMetadata save(ReportMetadata metadata);

    Optional<ReportMetadata> ofIdentity(String reportId);

    List<ReportMetadata> findAll();
}
