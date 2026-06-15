package pt.isep.sem4pi.application.service.reporting;

import eapli.framework.domain.repositories.TransactionalContext;
import pt.isep.sem4pi.application.viadto.GeneratedReportDTO;
import pt.isep.sem4pi.domain.model.reporting.ReportMetadata;
import pt.isep.sem4pi.domain.repositories.ReportMetadataRepository;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class GenerateMonthlyReportService {

    // DEFESA:
    // Este serviço coordena interfaces pequenas para manter baixo acoplamento entre recolha, construção e formato.
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ReportDataCollector<MonthlyStatisticsData> collector;
    private final MonthlyStatisticsReportBuilder reportBuilder;
    private final ReportRenderer renderer;
    private final RepositoryFactory repositoryFactory;
    private final Path outputDirectory;
    private final Clock clock;

    public GenerateMonthlyReportService(
            final ReportDataCollector<MonthlyStatisticsData> collector,
            final MonthlyStatisticsReportBuilder reportBuilder,
            final ReportRenderer renderer,
            final RepositoryFactory repositoryFactory,
            final Path outputDirectory,
            final Clock clock) {
        this.collector = collector;
        this.reportBuilder = reportBuilder;
        this.renderer = renderer;
        this.repositoryFactory = repositoryFactory;
        this.outputDirectory = outputDirectory;
        this.clock = clock;
    }

    public GeneratedReportDTO generate(final YearMonth period) throws IOException {
        final LocalDateTime generatedAt = LocalDateTime.now(clock);
        final MonthlyStatisticsData data = collector.collect(period);
        final MonthlyStatisticsReport report =
                reportBuilder.build(period, generatedAt, data);
        final String reportId = "RPT-" + UUID.randomUUID();
        final String fileName = "%s-%s-%s-%s.%s".formatted(
                report.type().toLowerCase().replace('_', '-'),
                period,
                generatedAt.format(FILE_TIMESTAMP),
                reportId,
                renderer.fileExtension());
        // DEFESA:
        // O renderer é uma variação protegida: outro formato pode ser injetado sem alterar o caso de uso.
        final Path renderedFile = renderer.render(report, outputDirectory.resolve(fileName));

        final ReportMetadata metadata = new ReportMetadata(
                reportId, report.type(), report.title(), period, generatedAt,
                renderer.format(), renderedFile.toString());
        persistMetadata(metadata, renderedFile);
        return new GeneratedReportDTO(
                metadata.reportId(), metadata.title(), metadata.period(),
                metadata.generatedAt(), metadata.format(), metadata.fileLocation());
    }

    private void persistMetadata(final ReportMetadata metadata,
                                 final Path renderedFile) throws IOException {
        // DEFESA:
        // A transação protege a persistência dos metadados; a remoção do ficheiro compensa uma falha da BD.
        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        try {
            tx.beginTransaction();
            final ReportMetadataRepository repository =
                    repositoryFactory.reportMetadata(tx);
            repository.save(metadata);
            tx.commit();
        } catch (final RuntimeException | Error exception) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Files.deleteIfExists(renderedFile);
            throw exception;
        } finally {
            tx.close();
        }
    }
}
